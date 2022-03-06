package guru.qa;

import com.codeborne.selenide.Configuration;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import java.util.Map;

import static com.codeborne.selenide.Condition.appear;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class DemowebshopTests {

    @Test
    public void addToCartAndDeleteTest(){
        RestAssured.baseURI = "http://demowebshop.tricentis.com/";
        Configuration.baseUrl = "http://demowebshop.tricentis.com/";

        // Достаем токены из метода авторизации
        Map<String, String> authCookie =
                given()
                        .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                        .formParam("Email", "qa.guru@ya.ru")
                        .formParam("Password", "Password")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(302)
                        .extract().cookies();

        String AuthToken = "NOPCOMMERCE.AUTH=" + authCookie.get("NOPCOMMERCE.AUTH") + ";";
        String ARRAffinity = "ARRAffinity=" + authCookie.get("ARRAffinity") + ";";
        String NopCustomer = "Nop.customer=" + authCookie.get("Nop.customer") + ";";

        // Логируемся на сайте с помощью подмены токена
        open("/Themes/DefaultClean/Content/images/logo.png");
        getWebDriver().manage().addCookie(new Cookie("NOPCOMMERCE.AUTH", authCookie.get("NOPCOMMERCE.AUTH")));
        open("");
        $(".account").shouldHave(text("qa.guru@ya.ru"));


        // Добавляем 10 товаров авторизированному участнику через апи
        for(int i = 0; i<10; i++) {
            given()
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .cookie(ARRAffinity)
                    .cookie(AuthToken)
                    .cookie(NopCustomer)
                    .body("product_attribute_72_5_18=53" +
                            "&product_attribute_72_6_19=54" +
                            "&product_attribute_72_3_20=57" +
                            "&addtocart_72.EnteredQuantity=1")
                    .when()
                    .post("http://demowebshop.tricentis.com/addproducttocart/details/72/1")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("success", is(true))
                    .body("message", is("The product has been added to your " +
                            "<a href=\"/cart\">shopping cart</a>"));
        }

        // Проверяем, что через апи добавлено 10 товаров
        refresh();
        $("#topcartlink .ico-cart").$(byText("(10)")).should(appear);

        // Удаляем через UI товары и проверяем, что их ноль
        $(".ico-cart .cart-label").click();
        $("[name='removefromcart']").click();
        $("[name='updatecart']").click();

        $("#topcartlink .ico-cart").$(byText("(0)")).should(appear);

        closeWebDriver();
    }


}
