package org.joget.marketplace;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.directory.model.Employment;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.StringUtil;
import org.joget.directory.model.Department;
import org.joget.directory.model.Organization;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.marketplace.model.Children;
import org.joget.marketplace.model.DepartmentNode;
import org.joget.marketplace.model.Parent;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.springframework.context.ApplicationContext;
import static org.joget.apps.datalist.model.DataListBinderDefault.USERVIEW_KEY_SYNTAX;

public class OrganizationalChart extends UserviewMenu implements PluginWebSupport {

    public static final String LAYOUTS = "horizontal";

    @Override
    public String getRenderPage() {
        Map<String, Object> freeMarkerModel = new HashMap<>();
        freeMarkerModel.put("request", getRequestParameters());
        freeMarkerModel.put("element", this);
        // Retrieve and add the color properties to the FreeMarker model
        freeMarkerModel.put("nodeTitleColor", getPropertyString("nodeTitleColor"));
        freeMarkerModel.put("nodeContentColor", getPropertyString("nodeContentColor"));
        freeMarkerModel.put("nodeParentColor", getPropertyString("nodeParentColor"));
        freeMarkerModel.put("nodeSiblingColor", getPropertyString("nodeSiblingColor"));
        freeMarkerModel.put("nodeChildrenColor", getPropertyString("nodeChildrenColor"));
        freeMarkerModel.put("TitleFontColor", getPropertyString("TitleFontColor"));
        freeMarkerModel.put("TitleContentColor", getPropertyString("TitleContentColor"));

        if (getPropertyString("dataSource").equals("jogetOrgChart")) {
            getJogetOrganizationChart();
        } else if (getPropertyString("dataSource").equals("formData")) {
            getFormData();
        }

        String content = "";
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        String renderingOption = getPropertyString("layouts");
        // Determine the FTL template based on the rendering option
        String templatePath;
        if (LAYOUTS.equals(renderingOption)) {
            templatePath = "/templates/orgchart.ftl"; // Use the first template path
        } else {
            templatePath = "/templates/orgChartVertical.ftl"; // Use the second template path by default
        }
        content = pluginManager.getPluginFreeMarkerTemplate(freeMarkerModel, getClass().getName(), templatePath, null);
        return content;
    }

    private void getJogetOrganizationChart() {
        ApplicationContext ac = AppUtil.getApplicationContext();
        ExtDirectoryManager directoryManager = (ExtDirectoryManager) ac.getBean("directoryManager");
        JSONArray jsArray = new JSONArray();
        Parent parent = new Parent();
        // Retrieve the selected organization ID from user input
        String selectedOrgId = getPropertyString("ORGID");
        Organization organization = directoryManager.getOrganization(selectedOrgId);

        if (organization != null) {
            String orgId = organization.getId();
            Map<String, DepartmentNode> nodesMap = new HashMap<>();
            Collection<Department> departments = directoryManager.getDepartmentListByOrganization(orgId, "name", Boolean.FALSE, 0, -1);

            // Build the hierarchical structure
            List<DepartmentNode> departmentNodes = new ArrayList<>();
            getDepartments(departments, departmentNodes, nodesMap, null, null);

            // Convert the hierarchical structure into the JSON format expected by OrgChart
            List<Children> childrenList = buildChildrenList(nodesMap, null);

            // Create the root parent object for the organization
            parent.setName("Organization");
            parent.setTitle(organization.getName());
            parent.setChildren(childrenList);

            Gson gson = new Gson();
            String orgChart = gson.toJson(parent);

            setProperty("nodes", jsArray); // Not used in the current context
            setProperty("datascource", orgChart);
        }
    }

    public void getDepartments(Collection<Department> departments, List<DepartmentNode> nodes, Map<String, DepartmentNode> nodesMap, String parentId, Employment inheritedHod) {
        ApplicationContext ac = AppUtil.getApplicationContext();
        ExtDirectoryManager directoryManager = (ExtDirectoryManager) ac.getBean("directoryManager");
        for (Department department : departments) {
            DepartmentNode node = new DepartmentNode();

            String hodName = "";
            User hodUser = null; // We'll store the HOD user object here for comparison
            Employment hod = department.getHod();
            if (hod != null) {
                hodUser = hod.getUser();
                hodName = hod.getUser().getFirstName() + " " + hod.getUser().getLastName();
            } else if (inheritedHod != null) { // If the current department has no HOD, use the inherited HOD
                hodUser = inheritedHod.getUser();
                hodName = inheritedHod.getUser().getFirstName() + " " + inheritedHod.getUser().getLastName();
            }
            Employment subDepartmentHod = (hod != null) ? hod : inheritedHod;

            String departmentId = department.getId();

            node.setId(departmentId);
            if (hod != null) {
                node.setTitle(hodName + " (HOD)");
            } else if (inheritedHod != null) {
                // Display the inherited HOD from the parent department
                hodUser = inheritedHod.getUser();
                hodName = inheritedHod.getUser().getFirstName() + " " + inheritedHod.getUser().getLastName();
                node.setTitle(hodName + " (HOD)"); // Updated to differentiate inherited HOD
            } else {
                node.setTitle("");
            }
            node.setName(department.getName());
            if (parentId != null) {
                node.setPid(parentId);
            }

            // Add users to the DepartmentNode
            Collection<User> usersInDepartment = directoryManager.getUserByDepartmentId(departmentId);
            if (!usersInDepartment.isEmpty()) {
                List<User> userObjects = new ArrayList<>();
                for (User user : usersInDepartment) {
                    if (hodUser == null || !user.getId().equals(hodUser.getId())) { // Exclude the HOD from the list
                        userObjects.add(user);

                    }
                }
                node.setUsers(userObjects);
            }

            if (nodesMap.containsKey(departmentId)) {
                DepartmentNode dnode = nodesMap.get(departmentId);
                String pId = dnode.getPid();
                if (pId == null || pId.isEmpty()) {
                    nodesMap.put(departmentId, node);
                }
            } else {
                nodesMap.put(departmentId, node);
            }
            nodes.add(node);

            Collection<Department> subDepartmentList = directoryManager.getDepartmentsByParentId("", departmentId, "name", Boolean.FALSE, 0, -1);
            if (!subDepartmentList.isEmpty()) {

                Employment currentDepartmentHod = department.getHod();
                subDepartmentHod = (currentDepartmentHod != null) ? currentDepartmentHod : subDepartmentHod;

                // Pass the updated subDepartmentHod to the recursive call
                getDepartments(subDepartmentList, nodes, nodesMap, departmentId, subDepartmentHod);
            }
        }
    }

    private List<Children> buildChildrenList(Map<String, DepartmentNode> nodesMap, String parentId) {
        List<Children> childrenList = new ArrayList<>();
        for (DepartmentNode node : nodesMap.values()) {
            if ((parentId == null && node.getPid() == null) || (parentId != null && parentId.equals(node.getPid()))) {
                Children child = new Children();
                child.setName(node.getName());
                child.setTitle(node.getTitle());

                Employment inheritedHod = null;

                // Check if the node has its own HOD
                Employment hod = node.getHod();

                if (hod == null) {
                    // If the current department doesn't have an HOD, try to inherit from the parent
                    DepartmentNode parentNode = nodesMap.get(node.getPid());
                    if (parentNode != null) {
                        Employment parentHod = parentNode.getHod();
                        if (parentHod != null) {
                            hod = parentHod; // Inherit HOD from the parent department
                            inheritedHod = parentHod;
                        }
                    }
                }
                // Set title based on HOD information
                if (hod != null) {
                    String hodName = hod.getUser().getFirstName() + " " + hod.getUser().getLastName();
                    child.setTitle(node.getTitle() + " (HOD: " + hodName + ")");
                } else if (inheritedHod != null) {
                    // Inherit HOD from the parent if the node itself doesn't have an HOD
                    child.setTitle(node.getTitle() + " (" + inheritedHod.getUser().getFirstName() + " " + inheritedHod.getUser().getLastName() + " - HOD - Inherited)");
                } else {
                    child.setTitle(node.getTitle());
                }

                child.setHodUser(node.getHodUser());
                child.setHod(node.getHod());

                List<Children> subChildren = buildChildrenList(nodesMap, node.getId());

                // Add users to the Children object if there are no sub-departments
                if (node.getUsers() != null && !node.getUsers().isEmpty()) {
                    List<Children> userChildren = new ArrayList<>();
                    for (User user : node.getUsers()) {
                        Children userChild = new Children();

                        Employment employment = (Employment) user.getEmployments().iterator().next();
                        String jobTitle = employment.getRole();

                        // Check if employment is not null before accessing job title
                        if (employment != null) {
                            // Include job title in the userChild title
                            userChild.setName(user.getFirstName() + " " + user.getLastName());
                            userChild.setTitle(jobTitle);
                        } else {
                            // If employment is null, include only the name in the title
                            userChild.setName(user.getFirstName() + " " + user.getLastName());
                            userChild.setTitle(user.getFirstName() + " " + user.getLastName());
                        }
                        userChildren.add(userChild);
                    }
                    child.setChildren(userChildren);
                }

                if (!subChildren.isEmpty()) {
                    child.setChildren(subChildren);
                }

                childrenList.add(child);
            }
        }
        return childrenList;
    }

    private void getFormData(){
        Parent parent = new Parent();
        List<Children> childrenList = new ArrayList<>();

        String departmentField = getPropertyString("departmentField");
        String nameField = getPropertyString("nameField");
        String titleField = getPropertyString("jobTitleField");
        String parentField = getPropertyString("parentIDField");
        String formDefId = getPropertyString("formDefId");
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        String tableName = appService.getFormTableName(AppUtil.getCurrentAppDefinition(), formDefId);
        FormRowSet data = getData(formDefId, tableName);

        for (FormRow r : data) {
            String departmentStr = r.getProperty(departmentField);
            String nameStr = r.getProperty(nameField);
            String titleStr = r.getProperty(titleField);
            String parentStr = r.getProperty(parentField);

            if (parentStr.isEmpty()) {
                Children child = new Children();
                child.setName(departmentStr);
                child.setTitle(nameStr + " (" + titleStr + ")");

                buildFormDataChildrenList(child, data, departmentStr, nameStr, titleStr);

                childrenList.add(child);
            }
        }

        ApplicationContext ac = AppUtil.getApplicationContext();
        ExtDirectoryManager directoryManager = (ExtDirectoryManager) ac.getBean("directoryManager");
        // Retrieve the selected organization ID from user input
        String selectedOrgId = getPropertyString("ORGID");
        Organization organization = directoryManager.getOrganization(selectedOrgId);

        // Create the root parent object for the organization
        parent.setName("Organization");
        parent.setTitle(organization.getName());
        parent.setChildren(childrenList);

        Gson gson = new Gson();
        String orgChart = gson.toJson(parent);

        setProperty("datascource", orgChart);

    }

    private void buildFormDataChildrenList(Children child, FormRowSet data, String departmentStr, String parentNameStr, String parentTitleStr) {
        List<Children> userChildren = new ArrayList<>();
        for (FormRow rChild : data) {
            String childDepartmentStr = rChild.getProperty(getPropertyString("departmentField"));
            String childNameStr = rChild.getProperty(getPropertyString("nameField"));
            String childTitleStr = rChild.getProperty(getPropertyString("jobTitleField"));
            String childParentStr = rChild.getProperty(getPropertyString("parentIDField"));
            if (childDepartmentStr.isEmpty()) {
                if (childParentStr != null && !childParentStr.isEmpty()) {
                    if (childParentStr.equals(departmentStr)) {
                        Children userChild = new Children();
                        userChild.setName(childNameStr);
                        userChild.setTitle(childTitleStr);
                        userChildren.add(userChild);
                    }
                }
            } else {
                if (childParentStr != null && !childParentStr.isEmpty()) {
                    if (childParentStr.equals(departmentStr)) {
                        Children userChild = new Children();
                        if (childNameStr.isEmpty() && childTitleStr.isEmpty()) {
                            userChild.setName(childDepartmentStr);
                            userChild.setTitle(parentNameStr + " (" + parentTitleStr + ")");
                            userChildren.add(userChild);
                        } else {
                            userChild.setName(childDepartmentStr);
                            userChild.setTitle(childNameStr + " (" + childTitleStr + ")");
                            userChildren.add(userChild);
                        }
                        buildFormDataChildrenList(userChild, data, childDepartmentStr, childNameStr, childTitleStr);
                    }
                }
            }
        }
        child.setChildren(userChildren);
    }

    /**
     * Retrieve all the form data based on selected form
     *
     * @return
     */
    protected FormRowSet getData(String formDefId, String tableName) {
        FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");

        String condition = "";
        Collection<String> params = new ArrayList<String>();

        String extraCondition = getPropertyString("extraCondition");
        String keyName = getPropertyString(Userview.USERVIEW_KEY_NAME);
        String keyValue = getUserview().getParamString("key");
        if (keyValue == null || Userview.USERVIEW_KEY_EMPTY_VALUE.equals(keyValue)) {
            keyValue = "";
        }

        if (extraCondition != null && extraCondition.contains(USERVIEW_KEY_SYNTAX)) {
            extraCondition = extraCondition.replaceAll(StringUtil.escapeRegex(USERVIEW_KEY_SYNTAX), StringUtil.escapeRegex(keyValue));
        } else if (keyName != null && !keyName.isEmpty() && keyValue != null && !keyValue.isEmpty()) {
            if (!condition.isEmpty()) {
                condition += " AND ";
            }
            condition += getFieldName(keyName) + " = ?";
            params.add(keyValue);
        }

        if (!extraCondition.isEmpty()) {
            if (!condition.isEmpty()) {
                condition += " AND ";
            }
            condition += extraCondition;
        }

        if (!condition.isEmpty()) {
            condition = " WHERE " + condition;
        }

        FormRowSet rows = formDataDao.find(tableName, tableName, condition, params.toArray(new String[0]), FormUtil.PROPERTY_DATE_CREATED, false, null, null);
        rows.setMultiRow(true);

        return rows;
    }

    /**
     * Get the field name with prefix
     *
     * @param name
     * @return
     */
    protected String getFieldName(String name) {
        if (name != null && !name.isEmpty() && !FormUtil.PROPERTY_ID.equals(name)
                && !(FormUtil.PROPERTY_CREATED_BY.equals(name)
                || FormUtil.PROPERTY_CREATED_BY_NAME.equals(name)
                || FormUtil.PROPERTY_MODIFIED_BY.equals(name)
                || FormUtil.PROPERTY_MODIFIED_BY_NAME.equals(name))) {
            name = FormUtil.PROPERTY_CUSTOM_PROPERTIES + "." + name;
        }
        name = "e." + name;
        return name;
    }

    @Override
    public String getCategory() {
        return "Marketplace";
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-chart-bar\"></i>";
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
    }

    @Override
    public String getDecoratedMenu() {
        return null;
    }

    @Override
    public String getName() {
        return "Organizational Chart";
    }

    @Override
    public String getVersion() {
        return "7.0.0";
    }

    @Override
    public String getDescription() {
        return "Organizational Chart that retrieves data from Joget Organization Chart or Form Data.";
    }

    @Override
    public String getLabel() {
        return "Organizational Chart";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/organizationalChart.json", null, true, "message/OrganizationalChart");
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN);
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    public boolean isDownloadAllowed(Map requestParameters) {
        // Allow download for authenticated users by default
        return !WorkflowUtil.isCurrentUserAnonymous();
    }

}
