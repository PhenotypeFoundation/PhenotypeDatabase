$.fn.showLayer = function(options){
	var defaults = {
		layerClass: '.popup',
		overlayOpacity: 0.5,
		auto: false
	};
	var options = $.extend(defaults, options);
	return this.each(function(){
		var that = $(this);
		that.vars = {
			overlay: null,
			layer: null
		};
		if(options.auto){

			var position = 'fixed';
			that.vars.overlay = $('#overlay');
			if(!that.vars.overlay.length){
				that.vars.overlay = $('<div id="overlay"></div>').appendTo(document.body);
			}
			that.vars.layer = $(options.layerClass);
			if(that.vars.layer){
				that.vars.overlay.css({
					'opacity': 0,
					'display': 'block',
					'position': 'fixed'
				}).stop(true).animate({
					'opacity': options.overlayOpacity
				}, 500);
				if($(window).height() > (that.vars.layer.outerHeight())){
					position = 'fixed';
					that.vars.layer.css({
						'position': position,
						'opacity': 0,
						'top': ($(window).height() - that.vars.layer.outerHeight()) / 2,
						'left': ($(window).width() - that.vars.layer.outerWidth()) / 2
					}).stop(true).delay(200).animate({
						'opacity': 1
					});
				}
				else if($(window).height() <= that.vars.layer.outerHeight()){
					position = 'absolute';
					that.vars.layer.css({
						'position': position,
						'opacity': 0,
						'top': $(window).scrollTop(),
						'left': ($(window).width() - that.vars.layer.outerWidth()) / 2
					}).stop(true).delay(200).animate({
						'opacity': 1
					});
				}

				that.vars.layer.find('.close').unbind('click').bind('click',function(e){
					//if(e) e.preventDefault();
					that.vars.layer.animate({
						'opacity': 0
					}).queue(function(){
						that.vars.layer.css({
							'top': -10000,
							'left': -10000
						});
						that.vars.layer.dequeue();
					});
					that.vars.overlay.delay(200).animate({
						'opacity': 0
					}).queue(function(){
						that.vars.overlay.css({
							'display': 'none'
						});
						that.vars.overlay.dequeue();
					});
				});

				$(window).resize(function(){
					if(that.vars.layer.css('opacity') == '1'){
						if($(window).height() > that.vars.layer.outerHeight()){
							position = 'fixed';
							that.vars.layer.css({
								'position': position,
								'top': ($(window).height() - that.vars.layer.outerHeight()) / 2,
								'left': ($(window).width() - that.vars.layer.outerWidth()) / 2
							});
						}
						else if($(window).height() <= that.vars.layer.outerHeight()){
							position = 'absolute';
							that.vars.layer.css({
								'position': position,
								'top': $(window).scrollTop(),
								'left': ($(window).width() - that.vars.layer.width()) / 2
							});
						}
						that.vars.overlay.css({
							'position': 'fixed'
						});
					}
				});
			}
		}else{
			that.click(function(e){
				e.preventDefault();
				var position = 'fixed';
				that.vars.overlay = $('#overlay');
				if(!that.vars.overlay.length){
					that.vars.overlay = $('<div id="overlay"></div>').appendTo(document.body);
				}
				that.vars.layer = $(options.layerClass);
				if(that.vars.layer){
					that.vars.overlay.css({
						'opacity': 0,
						'display': 'block',
						'position': 'fixed'
					}).stop(true).animate({
						'opacity': options.overlayOpacity
					}, 500);

					if($(window).height() > (that.vars.layer.outerHeight())){

						position = 'fixed';
						that.vars.layer.css({
							'position': position,
							'opacity': 0,
							'top': (($(window).height() - that.vars.layer.outerHeight()) / 2),
							'left': ($(window).width() - that.vars.layer.outerWidth()) / 2
						}).stop(true).delay(200).animate({
							'opacity': 1
						});

					}
					else if($(window).height() <= (that.vars.layer.outerHeight())){
						position = 'absolute';
						that.vars.layer.css({
							'position': position,
							'opacity': 0,
							'top': $(window).scrollTop(),
							'left': ($(window).width() - that.vars.layer.outerWidth()) / 2
						}).stop(true).delay(200).animate({
							'opacity': 1
						});

					}

					that.vars.layer.find('.close').unbind('click').bind('click',function(e){
						//if(e) e.preventDefault();
						that.vars.layer.animate({
							'opacity': 0
						}).queue(function(){
							that.vars.layer.css({
								'top': -10000,
								'left': -10000
							});
							that.vars.layer.dequeue();
						});
						that.vars.overlay.delay(200).animate({
							'opacity': 0
						}).queue(function(){
							that.vars.overlay.css({
								'display': 'none'
							});
							that.vars.overlay.dequeue();
						});
					});
				}
				$(window).resize(function(){
					if(that.vars.layer.css('opacity') == '1'){
						if($(window).height() > that.vars.layer.outerHeight()){
							position = 'fixed';
							that.vars.layer.css({
								'position': position,
								'top': ($(window).height() - that.vars.layer.outerHeight()) / 2,
								'left': ($(window).width() - that.vars.layer.outerWidth()) / 2
							});
						}
						else if($(window).height() <= (that.vars.layer.outerHeight())){
							position = 'absolute';
							that.vars.layer.css({
								'position': position,
								'top': $(window).scrollTop(),
								'left': ($(window).width() - that.vars.layer.width()) / 2
							});
						}
						that.vars.overlay.css({
							'position': 'fixed'
						});
					}
				});
			});
		}
	});
};
$(document).ready(function($){
	//sub menu
    $(".topnav > li").hover (function() {
        if ($(this).find("a:first-child").next().length) {
            $(this).children("a:first-child").addClass("hover");
			$(this).find(".subnav").css("overflow","visible");
            $(this).find(".subnav").stop().css("display","block").animate({
                height: $(this).find("ul").outerHeight()
            }, 500);
        }

    });
    $(".topnav > li").mouseleave (function() {
        if ($(this).find("a:first-child").next().length) {
            $(this).children("a:first-child").removeClass("hover");
			$(this).find(".subnav").css("overflow","visible");
            $(this).find(".subnav").stop().css("display","none").animate({
                height: 0
            }, 500);
        }
    });
	$(".topnav > li ul > li.has-child").each(function(){
		var w = $(this).parent().parent("div").width();
		$(this).find(".subsubnav").css("left",w);
		$(this).find(".subsubnav").css("overflow","visible");
	 	$(this).hover (function() {
			if ($(this).find("a:first-child").next().length) {
				$(this).children("a:first-child").addClass("hover");
				$(this).children(".subsubnav").stop().css("display","block").animate({
					width: 210
					//width: $(this).find("ul").outerWidth()
				}, 1000);
			}
		});
	   	$(this).mouseleave (function() {
			if ($(this).find("a:first-child").next().length) {
				$(this).children("a:first-child").removeClass("hover");
				$(this).find(".subsubnav").css("overflow","visible");
				$(this).children(".subsubnav").stop().css("display","none").animate({
					width: 0
				}, 1000);
			}
		});
	});
	//show login popup
	$('.loginBlock .signup, .loginBlock .login').showLayer({
		layerClass: '.loginPopup',
		overlayOpacity: 0.2
	});
	//show more content
	$(".toggleCont .more").each(function(){
		$(this).click(function(event) {
     		event.preventDefault();
			$(this).parent().nextAll(".fullCont").first().slideToggle();
			$(this).css("visibility", "hidden");

    	});
	});
	$(".toggleCont .less").each(function(){
		$(this).click(function(event) {
     		event.preventDefault();
			$(this).parents(".fullCont").slideToggle();
			$(this).parents().find(".more").css("visibility", "visible");

    	});
	});
	//submenu
	$(".moduleContainer .nav").each(function(){
		$(".moduleContainer .nav > li").mouseover (function() {
			if ($(this).find("a:first-child").next().length) {
				$(this).find("a:first-child").addClass("hover");
				$(this).find(".subnav").stop().css("display","block").animate({
					height: $(this).find("ul").outerHeight()
				}, 500);
			}
		});
		$(".moduleContainer .nav > li").mouseleave (function() {
			if ($(this).find("a:first-child").next().length) {
				$(this).find("a:first-child").removeClass("hover");
				$(this).find(".subnav").stop().css("display","none").animate({
					height: 0
				}, 500);
			}
		});
	});
	//add class last, first for li
	$("ul").each(function(){
		 $("li",this).first().addClass("first");
		 $("li",this).last().addClass("last");
	});
	//tooltip
	$(".tooltip").each(function(){
		$(this).tooltip({ position: { my: "left+5 center", at: "right center" ,relative:true } });
	});
	$(".datalist").each(function(){
		$(this).css("height",$("table",this).outerHeight());
		$(this).customScrollbar({
		skin: "default-skin",
  		vScroll: false,
  		updateOnWindowResize: true});
	});

/*  Interferes with StudyWizard
	// place holder
	$("input[type='text'],textarea").each(function(){
		$(this).placeholder();
		$(this).focus(function() {
			$(this).addClass("focus");
		});
		$(this).blur(function() {
			$(this).removeClass("focus");
		});
	});
	$("input[type='password']").each(function(){
		$(this).focus(function() {
			$(this).addClass("focus");
		});
		$(this).blur(function() {
			$(this).removeClass("focus");
		});
	});
	//custom checkbox
	$("input[type='checkbox']").each(function(){
		$(this).iCheck();
	});
	//custom radio
	$("input[type='radio']").each(function(){
		$(this).iCheck();
	});
	//accordion
	$(".accordion").each(function(){
		$(this).accordion({
			heightStyle: "content"
		});
	});
	//datepicker
	$(".datepicker").each(function(){
		$(this).datepicker({
			dateFormat: "dd-mm-yy",
			showOn: "both",
			buttonImage: "assets/images/blank.gif",
			buttonImageOnly: true
		});
	});
	//custom select box
	$("select").each(function(){
		 $(this).selectBox();
	});
*/
});
$(window).load(function() {

});
