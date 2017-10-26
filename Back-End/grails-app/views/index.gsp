<!doctype html>
<html ng-app="fuse">
<head>
    <base href="/adminpanel">
    <meta charset="utf-8">
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>Adminpanel/ECM</title>
   <asset:stylesheet src="application.css"/>
</head>
<!--[if lt IE 10]>
<p class="browsehappy">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade
  your browser</a> to improve your experience.</p>
<![endif]-->
<body md-theme="{{vm.themes.active.name}}" md-theme-watch="" ng-controller="IndexController as vm"
      class="{{state.current.bodyClass || ''}}">
<ms-splash-screen id="splash-screen">
    <div class="center">
        <div class="logo"><span>EF</span></div>
        <div class="spinner-wrapper">
            <div class="outer-spinner">
                <div class="spinner">
                    <div class="left">
                        <div class="circle"></div>
                    </div>
                    <div class="right">
                        <div class="circle"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</ms-splash-screen>
<div id="main" class="animate-slide-up" ui-view="main" layout="column"></div>
<ms-theme-options></ms-theme-options>
<asset:javascript src="application.js"/>
</body>
</html>