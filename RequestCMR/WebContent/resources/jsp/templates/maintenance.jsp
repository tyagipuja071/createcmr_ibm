<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<style>

</style>
<div class="ibm-columns">
	<!-- Main Content -->
	<div class="ibm-col-1-1">
		<div id="wwq-content">
			<p class="ibm-error">
				<strong>
				System maintenance is ongoing. Please visit the page again after the 
				scheduled maintenance period from 
				<%=SystemConfiguration.getParameter("MAINTENANCE_START").getValue()%>
				to <%=SystemConfiguration.getParameter("MAINTENANCE_END").getValue()%> 
				<%=SystemConfiguration.getParameter("TIMEZONE").getValue()%>.
				</strong>
			</p>
			
		</div>
	</div>
</div>
