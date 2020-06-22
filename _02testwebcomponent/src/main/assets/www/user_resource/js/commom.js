
// 封装选择器
function $$(selector) {
    return document.querySelector(selector);
}

// 封装选择器(ALL)
function $$$(selector) {
    return document.querySelectorAll(selector);
}

// 获取query
function getQuery(name) {
    var queryReg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)', 'i'),
        queryStr = location.search.substr(1).match(queryReg);
    if (queryStr != null) {
        return unescape(queryStr[2]);
    }
    return '';
}
