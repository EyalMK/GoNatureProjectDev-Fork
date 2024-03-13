package EmployeesControllers;

import CommonClient.controllers.BaseController;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ParkEmployeeDashboardPageController extends BaseController {

    @FXML
    private MFXButton btnAvailableSpot;

    @FXML
    private MFXButton btnGenerateBill;

    @FXML
    void OnClickAvailableSpotButton(ActionEvent event)
    {
        String checkAvailableSpotPage = "/EmployeesUI/CheckAvailableSpotPage.fxml";
        applicationWindowController.setCenterPage(checkAvailableSpotPage);
    }

    @FXML
    void OnClickGenerateBillButton(ActionEvent event)
    {
        String generateBillPage = "/EmployeesUI/GenerateBill";
        applicationWindowController.setCenterPage(generateBillPage);
    }

}
