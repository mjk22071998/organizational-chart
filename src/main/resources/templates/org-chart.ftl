<script src="${request.contextPath}/plugin/org.joget.marketplace.OrganizationalChart/others/orgchart.js"></script>


<fieldset id="form-canvas">
<div class="form-section">
<div class="form-section-title"><span>ORG CHART</span></div>
    <div id="tree"></div>
</div>
</div>
</fieldset>

<script>
    
    $(document).ready(function(){
        var chart = new OrgChart(document.getElementById("tree"), {
            template: "isla",
            mouseScrool: OrgChart.action.none,
            nodeBinding: {
                field_0: "name"
            },
            enableSearch: false,
            enableDragDrop: false,
            nodes: ${element.properties.nodes}
        });
    });
    
</script> 