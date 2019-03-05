<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<f:view>
	<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Soniya</title>
</head>
<body>
	<h1>JSP page</h1>
	<h:form>
		<h:panelGrid columns="1">
			<h:outputLabel for="query">Enter Search: </h:outputLabel>
			<h:inputText value="#{searchBean.query}"></h:inputText>
			<br />
			<br />

			<h:commandButton value="Submit" action="#{searchBean.doSearch()}"></h:commandButton>

		</h:panelGrid>
	</h:form>
	<c:forEach items="${searchBean.results}" var="result">
		<br/>
		<tr>
			<td>Document ID: <c:out value="${result}" /></td>
		</tr>
		<br/>
	</c:forEach>

</body>
	</html>
</f:view>