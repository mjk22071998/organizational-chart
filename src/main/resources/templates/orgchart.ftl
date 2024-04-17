<#if element.properties.ORGID?has_content>

<script src="${request.contextPath}/plugin/org.joget.marketplace.OrganizationalChart/orgchart/ajaxcdn.js"></script>
<script src="${request.contextPath}/plugin/org.joget.marketplace.OrganizationalChart/orgchart/html2canvas.min.js"></script>
<script src="${request.contextPath}/plugin/org.joget.marketplace.OrganizationalChart/orgchart/jspdf.umd.min.js"></script>
<script src="${request.contextPath}/plugin/org.joget.marketplace.OrganizationalChart/others/orgchart.js"></script>
<link rel="icon" href="img/logo.png">
<link rel="stylesheet" href="${request.contextPath}/plugin/org.joget.marketplace.OrganizationalChart/orgchart/jquery.orgchart.css">
<link rel="stylesheet" href="${request.contextPath}/plugin/org.joget.marketplace.OrganizationalChart/orgchart/style.css">
<link rel="stylesheet" href="${request.contextPath}/plugin/org.joget.marketplace.OrganizationalChart/orgchart/orgChartHorizontal.css">





<style type="text/css">
  .orgchart .node .title {
     background-color: ${nodeTitleColor};
     
     color: ${TitleFontColor};
  }
  .orgchart .node .content {
     background-color: ${nodeContentColor};
    
     color:${TitleContentColor};
  }
  .orgchart .node.highlight-parent .title,
  .chart-legend_item_color.highlight-parent {
     background-color: ${nodeParentColor};
}
  .orgchart .node.highlight-siblings .title,
  .chart-legend_item_color.highlight-siblings {
     background-color: ${nodeSiblingColor};

}
  .orgchart .node.highlight-children .title, 
  .chart-legend_item_color.highlight-children {
     background-color: ${nodeChildrenColor};
}
 
</style>


<#if element.properties.enableParent == 'true'>
  <!-- Your code here for the case where enableParent is not 'true' -->
  <div class="chart-legend">
    <div class="chart-legend__item">
      <div class="chart-legend_item_color highlight-parent"></div>
      <div class="chart-legend_item_title">Parent</div>
    </div>
    <div class="chart-legend__item">
      <div class="chart-legend_item_color highlight-siblings"></div>
      <div class="chart-legend_item_title">Siblings</div>
    </div>
    <div class="chart-legend__item">
      <div class="chart-legend_item_color highlight-children"></div>
      <div class="chart-legend_item_title">Children</div>
    </div>
  </div>
</#if>

<fieldset id="form-canvas">
<div class="form-section">
<div class="form-section-title"><span>ORG CHART</span></div>
    <div id="chart-container"></div>
</div>
</div>
</fieldset>

<script>
    var datasrc = ${element.properties.datascource};
    console.log(datasrc);
</script>

<script type="text/javascript" src="${request.contextPath}/plugin/org.joget.marketplace.OrganizationalChart/orgchart/jquery.orgchart.js"></script>
  <script type="text/javascript">
    $(function() {
   var datascource = ${element.properties.datascource};
   var oc= $('#chart-container').orgchart({
      'data' : datascource,
      'nodeContent': 'title',
      'visibleLevel': 3,
      <#if element.properties.collapsed! == 'false'>
      'visibleLevel': 999,
      </#if>
      'chartClass': '',
      'parentNodeSymbol': '',
      'draggable': false,
      'direction': 't2b',
     <#if element.properties.panZoom! == 'true'>
      'zoom':'true',
      'pan':'true'
     </#if>
    });

    oc.$chartContainer.on('touchmove', function(event) {
       event.preventDefault();
    });

 // Scroll the .orgchart container to 50% horizontally and vertically
    var orgchartContainer = $('.orgchart');
    orgchartContainer.scrollLeft(orgchartContainer.width() / 2);
    orgchartContainer.scrollTop(orgchartContainer.height() / 2);

// Function to center the org chart horizontally
    function centerOrgChartHorizontally() {
        var chartContainer = $('#chart-container');
        var chartWidth = chartContainer.find('.orgchart').outerWidth();
        var containerWidth = chartContainer.width();
        var scrollLeftPosition = (chartWidth - containerWidth) / 2;
        chartContainer.scrollLeft(scrollLeftPosition);
    }

    // Call the function to center the chart after it's initialized
    centerOrgChartHorizontally();

function adjustChartContainer() {
    var chartContainer = $('#chart-container');
    var hasHorizontalOverflow = chartContainer.get(0).scrollWidth > chartContainer.width();
    var hasVerticalOverflow = chartContainer.get(0).scrollHeight > chartContainer.height();

    // If there's no overflow, center the content
    if (!hasHorizontalOverflow && !hasVerticalOverflow) {
        chartContainer.css('justify-content', 'center');
    } else {
        // If there's overflow, align content to the start
        chartContainer.css('justify-content', 'flex-start');
    }
}
   // Adjust the container after the org chart is initialized
    adjustChartContainer();

    // Recheck when the window is resized
    $(window).resize(adjustChartContainer);



<#if element.properties.enableParent == 'true'>
    oc.$chart.find('.node')
      .on('mouseenter', function() {
        oc.getParent($(this)).addClass('highlight-parent');
        oc.getSiblings($(this)).addClass('highlight-siblings');
        oc.getChildren($(this)).addClass('highlight-children');
      })
      .on('mouseleave', function () {
        oc.$chart.find('.highlight-parent, .highlight-siblings, .highlight-children')
        .removeClass('highlight-parent highlight-siblings highlight-children');
      });
</#if>


    });

// Function to adjust the title width based on the content width
function adjustTitleWidth() {
  var contentElements = document.querySelectorAll('.orgchart .node .content');
  var titleElements = document.querySelectorAll('.orgchart .node .title');

  for (var i = 0; i < contentElements.length; i++) {
    var contentWidth = contentElements[i].offsetWidth;
    titleElements[i].style.width = contentWidth + 'px';
  }
}

// Call the function to adjust title widths when needed (e.g., on page load)
adjustTitleWidth();


</script>
</#if>