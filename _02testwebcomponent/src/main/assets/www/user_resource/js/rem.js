/**
 * 移动端rem自适应
 * 
 * @description 传入设计稿宽度，按照设计稿长度除以100，例如设计稿 50px 写作 .50rem
 * @param designWidth（设计稿宽度,例如：320、375、640、750）
 */

function remInit(designWidth) {
    
    var UA = navigator.userAgent,
        isAndroid = /android|adr/gi.test(UA),
        isIos = /iphone|ipod|ipad/gi.test(UA) && !isAndroid;

    var docEl = document.documentElement;

    var refreshRem = function() {
            var w = docEl.getBoundingClientRect().width || designWidth;
            var fs = w / designWidth * 100;

            if(w > 640) { // PC下适用，768、960、1360统一尺寸
                fs = 375 / designWidth * 100 * 1.2
            }

            docEl.style.fontSize = fs + 'px';

            window.fontSize = fs; //将fontSize公开到window下
        },
        refreshRemId;

    window.addEventListener('resize', function() {
        clearTimeout(refreshRemId);
        refreshRemId = setTimeout(refreshRem, 100);
    }, false);

    window.addEventListener('pageshow', function(e) {
        if (e.persisted) {
            clearTimeout(refreshRemId);
            refreshRemId = setTimeout(refreshRem, 100);
        }
    }, false);

    refreshRem();
}