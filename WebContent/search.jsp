<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<f:view>
	<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/styles.css" rel="stylesheet" type="text/css">
<link href="css/bootstrap.css" rel="stylesheet" type="text/css">
<link href="css/bootstrap.min.css" rel="stylesheet" type="text/css">
<title>Goggle</title>
</head>

<body>
	<div class="container">
		<div class="jumbotron">
			<table>
				<tr>
					<td><h1 class="display-4 text-primary">So</h1></td>
					<td><h1 class="display-4 text-success">ni</h1></td>
					<td><h1 class="display-4 text-danger">ya</h1></td>
				</tr>
			</table>
			<p class="text-muted">
				<b>So</b>ham . Mo<b>ni</b>sh . Adit<b>ya</b>
			</p>
			<hr class="my-4">
			<h:form>
				<div class="input-group">
					<h:inputText styleClass="form-control addpadding"
						value="#{searchBean.query}">
					</h:inputText>
					<div class="input-group-btn">
						<h:commandButton styleClass=" btn btn-primary addpadding"
							value="Submit" action="#{searchBean.searchQuery()}"
							onclick="searchClick()">
						</h:commandButton>
					</div>
				</div>
				<c:if test="${searchBean.totalResults > 0 }">
					<p class="text-muted">Found ${searchBean.totalResults} results
						in ${searchBean.timeTaken}s</p>
				</c:if>
				<c:forEach items="${searchBean.resultObjs}" var="result">
					<hr class="my-6">
					<table>
						<tr>
							<td><a target="_blank" href="${result.url}">${result.title}</a></td>
						</tr>
						<tr class="text-success">
							<td>Document ID: ${result.docID}</td>
						</tr>
						<%-- 						<tr class="text-muted snippet">
							<td>...${result.snippet}...</td>
						</tr> --%>
					</table>
				</c:forEach>
				<c:if test="${searchBean.doPaginate eq true}">
					<nav id="navbar" class="d-flex justify-content-center"
						aria-label="Page navigation example">
						<ul class="pagination">
							<li class="page-item"><h:commandButton
									disabled="#{searchBean.page eq 0}" styleClass="page-link"
									value="Previous" action="#{searchBean.goToPrev()}">
								</h:commandButton></li>
							<li class="page-item"><h:commandButton
									disabled="#{searchBean.page eq searchBean.totalPages}"
									styleClass="page-link" value="Next"
									action="#{searchBean.goToNext()}">
								</h:commandButton></li>
						</ul>
					</nav>
				</c:if>
				<c:if test="${searchBean.noResults eq true}">
					<hr class="my-6">
					<p class="text-center">No Results Found</p>
					<hr class="my-6">
				</c:if>
			</h:form>
		</div>
	</div>
	<div>
		<input type="hidden" id="paginate" value="${searchBean.doPaginate}">
	</div>
	<script type="text/javascript" src="js/bootstrap.js"></script>
	<script type="text/javascript" src="js/jquery.js"></script>
	<script type="text/javascript" src="js/search.js"></script>
</body>

	</html>
</f:view>
