console.log("this is script file");

const togglesidebar = () => {
    if ($('.sidebar').is(':visible')) {
        // true → sidebar is visible → hide it
        $('.sidebar').css("display", "none");
        $('.content').css("margin-left", "0%");
    } else {
        // false → sidebar is hidden → show it
        $('.sidebar').css("display", "block");
        $('.content').css("margin-left", "20%");
    }
};
