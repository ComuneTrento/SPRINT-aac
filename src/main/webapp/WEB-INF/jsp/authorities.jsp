<%--

       Copyright 2012-2013 Trento RISE

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

--%>
<%@page import="java.util.Map"%>
<%@page contentType="text/html" pageEncoding="UTF8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<fmt:setBundle basename="resources.internal" var="res" />
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">
<link href="../css/style.css" rel="stylesheet" type="text/css">
<title>Smart Community Authentication</title>
<style type="text/css">
.button-row {
	margin-top: 10px;
}

/** input within button-row class**/
.button-row input:hover {
	background-color: #b6bd00;
	color: white;
	text-transform: uppercase
}

.button-row input {
	background-color: #b6bd00;
	color: white;
	text-transform: uppercase
}

.button {
	background-color: #b6bd00;
	color: white;
	height: 40px;
	text-transform: uppercase
}

a.link {
	/*display: inline-block;
	font-family: "Roboto", sans-serif;
	font-size: 1.1em;*/
	padding: 0px 3px;
	/*text-decoration: none;
	width: auto*/
}

.form-group input {
	border-radius: 6px;
	margin-bottom: 10px;
	height: 30px;
}
</style>


</head>
<body>
	<%@ page language="java" import="java.util.*"%>
	<%@ page import="java.util.ResourceBundle"%>
	<%
		ResourceBundle resource = ResourceBundle.getBundle("commoncore");
		String internalAccountEnabled = resource.getString("internal.account.enabled");
	%>
	<img class="logo" src="../img/ls_logo.png" alt="SmartCommunity" />
	<div class="clear"></div>
	<div class="authorities">
		<%
			Map<String, String> authorities = (Map<String, String>) request.getAttribute("authorities");
			if (!authorities.isEmpty()) {
				if (internalAccountEnabled.equalsIgnoreCase("true")) {
		%>
		<div role="form">
			<form:form method="POST" acceptCharset="UTF-8"
				action="/aac/internal/login">
				<div class="form-group">
					<input id="username" type="text" name="username"
						placeholder="insert your email" />
					<script>
						document.getElementById('username').placeholder = "<fmt:message bundle="${res}" key='lbl_user'/>";
					</script>
					<div>
						<input id="password" type="password" name="password"
							placeholder="password" />
						<script>
							document.getElementById('password').placeholder = "<fmt:message bundle="${res}" key='lbl_pwd'/>";
						</script>
					</div>
					<a class="link" href=<%="/aac/internal/reset"%> target="_blank"><fmt:message
							bundle="${res}" key="lbl_pwd_reset" /></a> 
					<a class="link" href=<%="/aac/internal/register"%> target="_blank"><fmt:message
							bundle="${res}" key="lbl_register" /></a>
					<div class="button-row">
						<input type="submit" name="login"
							value="<fmt:message bundle="${res}" key="lbl_login" />"
							class="btn btn-default" />
					</div>
					<%
						if (request.getSession().getAttribute("userAbsent") != null) {
					%>
					<div>
						<label style="color: red;"> <fmt:message bundle="${res}"
								key="InvalidPasswordException" />
						</label>
					</div>
					<%
						}
					%>
				</div>
			</form:form>
		</div>
		<%
			}
			}
		%>

		<p>Please choose the provider for your login</p>
		<ul class="pprovider">
			<%
				for (String s : authorities.keySet()) {
					if (s.equalsIgnoreCase("internal"))
						continue;
			%>
			<li><a href="<%=request.getContextPath()%>/eauth/<%=s%>"><%=s.toUpperCase()%></a>
			</li>
			<%
				}
			%>
		</ul>
	</div>
</body>
</html>
