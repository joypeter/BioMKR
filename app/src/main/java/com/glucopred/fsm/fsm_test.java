package com.glucopred.fsm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import au.com.ds.ef.EasyFlow;
import au.com.ds.ef.EventEnum;
import au.com.ds.ef.StateEnum;
import au.com.ds.ef.StatefulContext;
import au.com.ds.ef.FlowBuilder;
import au.com.ds.ef.call.ContextHandler;

/**
 * Created by peter on 4/6/16.
 */



public class fsm_test {


    private static class FlowContext extends StatefulContext {
        private int balance = 1000;
        private String pin;
        private int invalidPinCounter;
        private int withdrawAmt;
    }

    public enum States implements StateEnum {
        SHOWING_WELCOME,
        WAITING_FOR_PIN,
        CHECKING_PIN,
        RETURNING_CARD,
        SHOWING_MAIN_MENU,
        SHOWING_PIN_INVALID,
        SHOWING_CARD_LOCKED,
        SHOWING_BALANCE,
        SHOWING_WITHDRAW_MENU,
        SHOWING_TAKE_CASH,
        TERMINATED
    }

    public enum Events implements EventEnum {
        cardPresent,
        cardExtracted,
        pinProvided,
        pinValid,
        pinInvalid,
        tryAgain,
        noMoreTries,
        cancel,
        confirm,
        menuShowBalance,
        menuWithdrawCash,
        menuExit,
        switchOff,
        cashExtracted
    }


    private EasyFlow<FlowContext> easyFlow;
    private IoController ioController;

    /*public static void main(String[] args) throws InterruptedException {
        Main m = new Main();

        m.initFlow();
        m.bindFlow();
        m.startFlow();

        Thread.sleep(Long.MAX_VALUE);
    }*/

    private void initFlow() {
        ioController = new IoController();
        ioController.init();

        easyFlow =

                FlowBuilder.from(States.SHOWING_WELCOME).transit(
                        FlowBuilder.on(Events.cardPresent).to(States.WAITING_FOR_PIN).transit(
                                FlowBuilder.on(Events.pinProvided).to(States.CHECKING_PIN).transit(
                                        FlowBuilder.on(Events.pinValid).to(States.SHOWING_MAIN_MENU).transit(
                                                FlowBuilder.on(Events.menuShowBalance).to(States.SHOWING_BALANCE).transit(
                                                        FlowBuilder.on(Events.cancel).to(States.SHOWING_MAIN_MENU)
                                                ),
                                                FlowBuilder.on(Events.menuWithdrawCash).to(States.SHOWING_WITHDRAW_MENU).transit(
                                                        FlowBuilder.on(Events.cancel).to(States.SHOWING_MAIN_MENU),
                                                        FlowBuilder.on(Events.confirm).to(States.SHOWING_TAKE_CASH).transit(
                                                                FlowBuilder.on(Events.cashExtracted).to(States.SHOWING_MAIN_MENU)
                                                        )
                                                ),
                                                FlowBuilder.on(Events.menuExit).to(States.RETURNING_CARD)
                                        ),
                                        FlowBuilder.on(Events.pinInvalid).to(States.SHOWING_PIN_INVALID).transit(
                                                FlowBuilder.on(Events.tryAgain).to(States.WAITING_FOR_PIN),
                                                FlowBuilder.on(Events.noMoreTries).to(States.SHOWING_CARD_LOCKED).transit(
                                                        FlowBuilder.on(Events.confirm).to(States.SHOWING_WELCOME)
                                                ),
                                                FlowBuilder.on(Events.cancel).to(States.RETURNING_CARD)
                                        )
                                ),
                                FlowBuilder.on(Events.cancel).to(States.RETURNING_CARD).transit(
                                        FlowBuilder.on(Events.cardExtracted).to(States.SHOWING_WELCOME)
                                )
                        ),
                        FlowBuilder.on(Events.switchOff).finish(States.TERMINATED)
                );
    }

    private void bindFlow() {
        easyFlow

                .whenEnter(States.SHOWING_WELCOME, new ContextHandler<FlowContext>() {
                    @Override
                    public void call(final FlowContext context) throws Exception {
                        ioController.println("\n\n*** Welcome ***\n---------------");
                        showOption("Select your option and press [Enter]...\n 1 Insert card\n 2 Terminate ATM", new IoController.InputEventHandler() {
                            @Override
                            public void onInputEvent(String s) {
                                try {
                                    if (s.equals("1")) {
                                        context.trigger(Events.cardPresent);
                                    } else {
                                        context.trigger(Events.switchOff);
                                    }
                                } catch (Exception e) {
                                    ;//TODO
                                }

                            }
                        });
                        context.invalidPinCounter = 0;
                    }
                });
    }

    private void startFlow() {
        easyFlow.start(new FlowContext());
    }

    private void showOption(String title, final IoController.InputEventHandler handler) {
        ioController.println(title);
        ioController.setEventHandler(handler);
    }
}
