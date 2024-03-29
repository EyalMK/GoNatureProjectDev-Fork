package EmployeesControllers;

import CommonClient.ClientUI;
import CommonClient.controllers.BaseController;
import CommonClient.controllers.OrderBillPageController;
import CommonUtils.CommonUtils;
import CommonUtils.ConfirmationPopup;
import CommonUtils.MessagePopup;
import Entities.Message;
import Entities.OpCodes;
import Entities.Order;
import Entities.OrderStatus;
import Entities.OrderType;
import client.ClientCommunicator;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Map;
import java.util.Objects;

import static CommonUtils.CommonUtils.isAllDigits;

public class GenerateBillController extends BaseController {
    @FXML
    private MFXButton btnGenerateBill;

    @FXML
    private Label lblErrorMsg;

    @FXML
    private MFXButton EnterParkButton;

    @FXML
    private Text successMsg;

    @FXML
    private TextField txtOrderID;

    private Order mostRecentOrder;

    public void cleanup() {
        txtOrderID.clear();
        lblErrorMsg.setText("");
        successMsg.setText("");
        EnterParkButton.setDisable(true);
        btnGenerateBill.setDisable(false);
    }

    @FXML
    void OnClickEnterParkButton(ActionEvent event) {
        if (mostRecentOrder == null) {
            lblErrorMsg.setText("Please generate a bill first");
            successMsg.setText("");
            return;
        }

        Message msg = new Message(OpCodes.OP_MARK_ORDER_AS_PAID, applicationWindowController.getUser().getUsername(), mostRecentOrder);
        ClientUI.client.accept(msg);

        Message response = ClientCommunicator.msg;
        OpCodes returnOpCode = response.getMsgOpcode();
        if (returnOpCode == OpCodes.OP_DB_ERR) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.DB_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }
        // Checking if the response from the server is inappropriate.
        if (returnOpCode != OpCodes.OP_MARK_ORDER_AS_PAID) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.SERVER_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }
        if (!(response.getMsgData() instanceof Boolean)) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.SERVER_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }

        if (!(Boolean) response.getMsgData()) {
            lblErrorMsg.setText("Failure. Order paid, expired or ineligible to be paid.");
            successMsg.setText("");
            return;
        }
        lblErrorMsg.setText("");
        successMsg.setText("Order marked as paid successfully");
    }

    @FXML
    void OnClickGenerateBillButton(ActionEvent event) {
        // Validate input of order ID... (positive numbers only)
        if (txtOrderID.getText().isEmpty() || !isAllDigits(txtOrderID.getText()) || Integer.parseInt(txtOrderID.getText()) <= 0) {
            successMsg.setText("");
            lblErrorMsg.setText("Please enter a valid order ID");
            return;
        }

        Message msg = new Message(OpCodes.OP_GET_ORDER_BY_ID, applicationWindowController.getUser().getUsername(), txtOrderID.getText());
        ClientUI.client.accept(msg);

        Message response = ClientCommunicator.msg;
        OpCodes returnOpCode = response.getMsgOpcode();

        if (returnOpCode == OpCodes.OP_DB_ERR) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.DB_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }
        // Checking if the response from the server is inappropriate.
        if (returnOpCode != OpCodes.OP_GET_ORDER_BY_ID) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.SERVER_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }
        Map<String, Object> results = (Map<String, Object>) response.getMsgData();
        Order order = (Order) results.get("order");
        if (!(order instanceof Order)) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.SERVER_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }

        boolean isPrepaid = (boolean) results.get("isPaid");

        mostRecentOrder = order;
        if (Objects.equals(mostRecentOrder.getOrderID(), "")) {
            lblErrorMsg.setText("Order not found");
            successMsg.setText("");
            return;
        }
        lblErrorMsg.setText("");
        successMsg.setText("Bill generated successfully");
        handleBillPresentation(mostRecentOrder, isPrepaid);
        switch (mostRecentOrder.getOrderStatus()) {
            case STATUS_WAITLIST:
            case STATUS_FULFILLED:
            case STATUS_PENDING_CONFIRMATION:
            case STATUS_CANCELLED:
            case STATUS_CONFIRMED_AND_ABSENT:
                lblErrorMsg.setText("Order status is not valid for generating a bill and entering the park");
                EnterParkButton.setDisable(true);
                break;
            case STATUS_ACCEPTED:
                String propmtMsg;
                if (mostRecentOrder.getOrderType() == OrderType.ORD_TYPE_GROUP) {
                    propmtMsg = "The order can be paid from the group guide user only.";
                } else {
                    propmtMsg = "Order status is not valid for generating a bill and entering the park";
                }
                lblErrorMsg.setText(propmtMsg);
                EnterParkButton.setDisable(true);
                break;
            case STATUS_CONFIRMED_PENDING_PAYMENT:
            case STATUS_SPONTANEOUS_ORDER_PENDING_PAYMENT:
                if (CommonUtils.isTimeBetween(mostRecentOrder.getEnteredTime(), mostRecentOrder.getExitedTime())) {
                    handleBillPresentation(mostRecentOrder, isPrepaid);
                } else {
                    EnterParkButton.setDisable(true);
                    lblErrorMsg.setText("Reservation time is not now.");
                }
                break;
            case STATUS_SPONTANEOUS_ORDER:
            case STATUS_CONFIRMED_PAID:
                if (CommonUtils.isTimeBetween(mostRecentOrder.getEnteredTime(), mostRecentOrder.getExitedTime())) {
                    EnterParkButton.setDisable(false);
                    lblErrorMsg.setText("Order is already paid and the visitor/s can enter the park.");
                } else {
                    EnterParkButton.setDisable(true);
                    lblErrorMsg.setText("Order is already paid and but the reservation time is not now.");
                }
                break;

        }
    }

    private void handleBillPresentation(Order order, boolean prepaid) {
        try {
            MessagePopup msg = new MessagePopup("/CommonClient/gui/OrderBillPage.fxml", 0, 0, true, false);
            OrderBillPageController controller = (OrderBillPageController) msg.getController();
            controller.setApplicationWindowController(applicationWindowController);
            msg.show(applicationWindowController.getRoot());

            controller.setMessagePopup(msg);
            controller.start(order, false, prepaid);
        } catch (Exception e) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.SERVER_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
        }
    }

    public void GenerateBillSpontaneousOrder(String orderID) {
        txtOrderID.setText(orderID);
        OnClickGenerateBillButton(null);
    }

    public void setOrderNum(String orderID) {
        txtOrderID.setText(orderID);
    }

    public void showPopUpAfterBillGeneration(boolean isBillGenerated, String orderID) {
        setOrderNum(orderID);
        String msgToPrompt;
        if (isBillGenerated) {
            msgToPrompt = "Order marked as paid successfully";
        } else {
            msgToPrompt = "Something went wrong. Please try again.";
        }
        MessagePopup popup = new MessagePopup(msgToPrompt, Duration.seconds(5), 300, 150, false);
        popup.show(applicationWindowController.getRoot());
        EnterParkButton.setDisable(!isBillGenerated);
        btnGenerateBill.setDisable(isBillGenerated);
    }

}
