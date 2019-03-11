<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<f:view>
	<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/bootstrap.css" rel="stylesheet" type="text/css">
<title>Goggle</title>
</head>
<body>
	<h5>When Google can't help, use.....</h5>
	<h1>Goggle</h1>
	<h4>Made in India</h4>
	<h:form>
		<h:panelGrid columns="1">
			<h:outputLabel for="query">Enter Search: </h:outputLabel>
			<h:inputText value="#{searchBean.query}"></h:inputText>
			<br />
			<br />

			<h:commandButton value="Submit" action="#{searchBean.searchQuery()}"></h:commandButton>

		</h:panelGrid>
	</h:form>
	<c:out value="Found ${searchBean.resultObjs.size()} results"/>
	<br/>
	<c:forEach items="${searchBean.resultObjs}" var="result">
		<br/>
		<tr>
			<td>Found in: <a target="_blank" href="${result.url}">${result.docID}</a></td>
		</tr>
		<br/>
	</c:forEach>
	
<script>
    window.onload = function(){
        console.log()
        }
    }
</script>

<script type="text/javascript" src="js/bootstrap.js"></script>
<script type="text/javascript" src="js/jquery.js"></script>
</body>
	</html>
</f:view>