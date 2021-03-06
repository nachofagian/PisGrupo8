<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
 	<jsp:include page="include/header.jsp" />
 	<link href="dist/libs/bootstrap/dist/css/bootstrap-dialog.min.css" rel="stylesheet">
 	 
</head>
<body>
	<div id="wrapper">
	    <jsp:include page="include/navigationBar.jsp"/>

	 	      <div id="page-wrapper">
            <div class="container-fluid" style="padding-bottom: 70px">
                <div class="row">
                    <div class="col-lg-12">
                        <h1 class="page-header">Create new packet</h1>
                    </div>
                </div>
                <div class="container-fluid">
                    <!-- Packet type -->
                    <div class="row">
                        <div class="col-xs-12">
                            <div class="panel panel-primary text-center">
                                <div class="panel-heading">
                                    <span class="left-side">Packet type</span>
                                </div>
                                <div class="panel-body">
                                    <form role="form" id="panelPacketDetails" class="text-left" style="margin-left:15px;">
                                        <div class="form-group">
                                            <div class="row">
                                                <div class="col-xs-3">
                                                    <label>Name:</label>
                                                    <input name="packetName" class="form-control">
                                                </div>
                                                <div class="col-xs-2">
                                                    <label style="display: block;">Packet type:</label>
                                                    <select name="packet-type" id="dropdown-packet-type">
                                                        <option name="packetTypeRadio" value="0">Index</option>
                                                        <option name="packetTypeRadio" value="1">Algoritmic</option>
                                                    </select>
                                                </div>
                                                <div class="col-xs-3">
                                                    <label>Categories:</label>
                                                    <select id="dropdown-packet-categories" name="packetCategories" multiple="multiple">
                                                      
                                                    </select>
                                                </div>
                                                <div class="col-xs-4 pull-left">
                                                    <button id="confirm-packet-details" type="button" class="btn btn-primary" style="margin-top:26px;">Confirm</button>
                                                </div>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- /.Packet type -->
                    <!-- Packet wizzard -->
                    <div id="packet-wizzard" style="display:none;">
                        <div class="row">
                            <div class="col-xs-12">
                                <ul class="nav nav-pills nav-justified thumbnail setup-panel">
                                    <li class="active">
                                        <a href="#step-1">
                                            <h4 class="list-group-item-heading">Step 1</h4>
                                            <p class="list-group-item-text">Select stocks</p>
                                        </a>
                                    </li>
                                    <li class="disabled">
                                        <a href="#step-2">
                                            <h4 class="list-group-item-heading">Step 2</h4>
                                            <p class="list-group-item-text">Select specific question</p>
                                        </a>
                                    </li>
                                    <li class="disabled hidden">
                                        <a href="#step-3">
                                            <h4 class="list-group-item-heading">Step 3</h4>
                                            <p class="list-group-item-text">write algorithm</p>
                                        </a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <!-- Step 1 -->
                        <div class="row setup-content well" id="step-1" style="display:none;">
                            <div class="col-md-4">
                                <div class="panel panel-info text-center">
                                    <div class="panel-heading">
                                        <span class="left-side"> Add new packet stock</span>
                                    </div>
                                    <div class="panel-body">
                                        <form id="panelStocks" class="text-left" role="form">
                                            <div class="form-group">
                                                <label>Stock name</label>
                                                <input class="form-control" name="stockName" type="text" id="input-search-stock" autocomplete="off" />
                                            </div>
                                            <div class="form-group">
                                                <label>Percentage</label>
                                                <div class="form-group input-group" style="width: 100px;">
                                                    <input type="text" class="form-control" name="stockPorcentage">
                                                    <span class="input-group-addon">%</span>
                                                </div>
                                            </div>
                                            <button type="button" id="btn-add-stock" class="btn btn-info pull-right">Add</button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-8">
                                <div class="panel panel-info">
                                    <div class="panel-heading text-center">
                                        Stock list
                                    </div>
                                    <!-- /.panel-heading -->
                                    <div class="panel-body">
                                        <div class="table-responsive" style="height: 185px;">
                                            <table class="table table-hover">
                                                <thead>
                                                    <tr>
                                                        <th>Symbol</th>
                                                        <th>Name</th>
                                                        <th>Porcentage</th>
                                                        <th>Delete</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                </tbody>
                                            </table>
                                        </div>
                                        <!-- /.table-responsive -->
                                    </div>
                                    <!-- /.panel-body -->
                                </div>
                            </div>
                            <div class="row pull-right">
                                <button id="go-next-from-step-1" type="button" class="btn btn-primary margin-right">Next</button>
                            </div>
                        </div>
                        <!-- /.Step 1 -->
                        <!-- Step 2 -->
                        <div class="row setup-content well" id="step-2" style="display:none;">
                            <div class="col-md-12">
                                <form class="text-left" role="form" id="panelSpecificQuestions">
                                    <div class="form-group">
                                        <div class="row">
                                            <div class="col-xs-9">
                                                <label>Specific question</label>
                                                <input id="input-question-text-eng" name="questionEng" class="form-control" placeholder="English question" />
                                                <input id="input-question-text-esp" name="questionEsp" class="form-control" placeholder="Spanish question" style="margin-top:10px;" />
                                            </div>
                                            <div class="col-xs-3">
                                                <button id="btn-create-question" type="button" class="btn btn-primary" style="margin-top:70px;">Create</button>
                                            </div>
                                        </div>
                                    </div>
                                </form>
                                <div class="panel-group" id="accordion">
                                </div>
                            </div>
                            <div class="row pull-right">
                                <button id="go-back-from-step-2" type="button" class="btn btn-primary">Back</button>
                                <button id="go-next-from-step-2" type="button" class="btn btn-primary margin-right">Next</button>
                            </div>
                        </div>
                        <!-- /.Step 2 -->
                        <!-- Step 3 -->
                        <div class="row setup-content well" id="step-3" style="display:none;">
                            <div class="col-md-12">
                                <div class="row">
                                    <div class="col-md-12">
                                        <div id="algorithm-code-editor">
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-12">
                                        <div id="algorithm-errors" class="hidden">
                                            Error en linea 1, falta ;</br>
                                            Error en linea 5, falta ;
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="row pull-right">
                                <button id="show-errors" type="button" class="btn btn-success">Check</button>
                                <button id="go-back-from-step-3" type="button" class="btn btn-primary">Back</button>
                                <button id="go-next-from-step-3" type="button" class="btn btn-primary margin-right">Confirm</button>
                            </div>
                            
                        </div>
                        <!-- /.Step 3 -->
                    </div>
                    <!-- /.Packet wizzard -->  
                    <h4 id="examples-header" style= "display: none" class="page-header">Examples</h4>
                    <div id="algorithm-examples" class="ace_editor ace-eclipse" style= "display: none; height:500px">
                            	VAR Float x ENDVAR <br>
                            	x = 8.5
                    </div>  
                </div>
            </div>
            <!-- /.row -->
        </div>
	 
	
	 	<jsp:include page="include/footer.jsp" />
	 	
	    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap3-dialog/1.34.5/js/bootstrap-dialog.min.js"></script>
	    <script src="dist/libs/ace/ace.js"></script>
	    <script src="dist/js/codeEditorAlgorithm.js"></script>
	    <script src="dist/libs/jquery/dist/jquery.validate.min.js"></script>
	      <script src="dist/libs/jquery/dist/additional-methods.min.js"></script>
	    <script src="dist/js/createPacketValidation.js"></script>
	   
	    <script src="dist/js/createPacket.js"></script>
	    <script src="dist/js/yahooAutocomplete.js"></script>
	    <script src="dist/libs/blockUI/jquery.blockUI.js"></script>
	    <script src="dist/js/testAlgorithm.js"></script>
	    
	</div>
</body>
</html>
