<!DOCTYPE html>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html lang="en">
	<head>
	</head>
	<body>
		<title>Water Auth SAML Login Success!</title>
		<body>
			<div>
				<h1>You're logged in</h1>
				<h3>User Details</h3>
				<div>${attributeHtml}</div>
			</div>
		</body>
</html>