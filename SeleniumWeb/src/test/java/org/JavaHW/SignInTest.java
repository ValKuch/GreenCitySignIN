package org.JavaHW;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.JavaHW.framework.tools.LocalStorageJS;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SignInTest {

    @FindBy(css = "app-header:nth-child(1) .ubs-header-sing-in-img")
    private WebElement signInButton;

    @FindBy(xpath = ".//app-sign-in/h1")
    private WebElement welcomeText;

    @FindBy(css = ".title-text")
    private WebElement welcomeText2;

    @FindBy(xpath = ".//app-sign-in/h2")
    private WebElement signInDetailsText;

    @FindBy(css = ".subtitle-text")
    private WebElement signInDetailsText2;

    @FindBy(xpath = ".//input[@id=\"email\"]/preceding-sibling::label")
    private WebElement emailLabel;

    @FindBy(id = "email")
    private WebElement emailInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(css = ".ubsStyle")
    private WebElement signInSubmitButton;

    @FindBy(css = ".alert-general-error")
    private WebElement errorMessage;

    @FindBy(xpath = "//*[@id=\"pass-err-msg\"]")
    private WebElement errorPassword;

    @FindBy(xpath = "//*[@id=\"email-err-msg\"]/app-error/div")
    private WebElement errorEmail;

    @FindBy(xpath = ".//*[@class=\"forgot-password-ubs\"]")
    private WebElement forgotPassword;

    @FindBy(css = ".ubs-send-btn")
    private WebElement passResetButton;

    @FindBy(css = ".ubs-link")
    private WebElement signUpNoAccount;

    @FindBy(css = ".ubs-user-name")
    private WebElement insideAccountUser;



    private static final String BASE_URL = "https://www.greencity.social/";
    private static final Long IMPLICITLY_WAIT_SECONDS = 10L;
    private static final Long ONE_SECOND_DELAY = 1000L;
    private static WebDriver driver;
    protected static LocalStorageJS localStorageJS;


    protected void presentationSleep() {
        presentationSleep(1);
    }

    protected void presentationSleep(int seconds) {
        try {
            Thread.sleep(seconds * ONE_SECOND_DELAY); // For Presentation ONLY
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @BeforeAll
    public static void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.get("https://www.greencity.social/");
        driver.manage().window().setSize(new Dimension(1264, 798));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICITLY_WAIT_SECONDS));
        localStorageJS = new LocalStorageJS(driver);
        System.out.println("@BeforeAll executed");

    }

    @BeforeEach
    public void initPageElements() {
        PageFactory.initElements(driver, this);
    }

    @BeforeEach
    public void setupThis() {
        driver.get(BASE_URL);
        System.out.println("\t@BeforeEach executed");
    }

    @AfterEach
    public void tearThis() {
        driver.manage().deleteAllCookies();
        localStorageJS.removeItemFromLocalStorage("accessToken");
        localStorageJS.removeItemFromLocalStorage("refreshToken");
        presentationSleep(4);
        driver.navigate().refresh();
        System.out.println("\t@AfterEach executed");}

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit(); // close()
        }
        System.out.println("@AfterAll executed");
    }

    @Test
    public void verifyTitle() {
        Assertions.assertEquals("GreenCity", driver.getTitle());
    }


    @DisplayName("Should display the username of the signed in user")
    @ParameterizedTest
    @CsvSource({
            "valerie.bucky69@gmail.com, 123456qQ#", //positive scenario
            "anotheruser@greencity.com, anotherpassword" //negative scenario

    })
    public void signIn(String email, String password) {
        signInButton.click();
        assertThat(welcomeText.getText(), is("Welcome back!"));
        assertThat(signInDetailsText.getText(), is("Please enter your details to sign in."));
        assertThat(emailLabel.getText(), is("Email"));
        emailInput.sendKeys(email);
        assertThat(emailInput.getAttribute("value"), is(email));
        passwordInput.sendKeys(password);
        assertThat(passwordInput.getAttribute("value"), is(password));
        signInSubmitButton.click();
        assertTrue(insideAccountUser.isEnabled());

    }

    @DisplayName("Should display a warning about the invalid email")
    @ParameterizedTest
    @MethodSource("invalidEmails")
    public void signInNotValid(String email) {
        signInButton.click();
        emailInput.sendKeys(email);
        passwordInput.sendKeys("uT346^^^erw");
        assertThat(errorEmail.getText(), is("Please check if the email is written correctly"));
    }
    private static Stream<String> invalidEmails() {
        return Stream.of(
                "samplestesgreencity.com",
                "@email.com",
                "some_email"
        );
    }


    @DisplayName("Should display a warning about an empty password")
    @ParameterizedTest
    @ValueSource(strings = {"Password must be at least 8 characters long without spaces"})
    public void passwordNotValid(String message) {
        signInButton.click();
        emailInput.sendKeys("samplestes@greencity.com");
        passwordInput.sendKeys(" ");
        signInSubmitButton.click();
        assertThat(errorPassword.getText(), is(message));

    }


    @DisplayName("Should display a warning about the incorrect password")
    @ParameterizedTest
    @MethodSource("IncorrectPasswords")
            public void passwordIncorrect(String password, String message) {
        signInButton.click();
        emailInput.sendKeys("samplestes@greencity.com");
        passwordInput.sendKeys(password);
        signInSubmitButton.click();
        assertThat(errorMessage.getText(), is(message));

    }
    private static Stream<Arguments> IncorrectPasswords() {
        return Stream.of(
                Arguments.of("tfcyuhgujhikj", "Bad email or password"),
                Arguments.of("12345678", "Bad email or password"),
                Arguments.of("#########", "Bad email or password")
        );
    }


    @DisplayName("Password reset option should be available")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.txt")

    public void forgotPassword(String email) {
        signInButton.click();
        forgotPassword.click();
        emailInput.sendKeys(email);
        assertTrue(passResetButton.isDisplayed());

    }

    @Test
    public void signUpInstead() {
        signInButton.click();
        signUpNoAccount.click();
        assertThat(welcomeText2.getText(), is("Hello!"));
        assertThat(signInDetailsText2.getText(), is("Please enter your details to sign up."));
    }


}
