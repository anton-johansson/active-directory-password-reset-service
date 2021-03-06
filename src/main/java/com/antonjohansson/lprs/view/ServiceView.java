/**
 * Copyright (c) Anton Johansson <antoon.johansson@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.antonjohansson.lprs.view;

import static com.antonjohansson.lprs.view.ServiceView.Stage.REQUEST_TOKEN;
import static com.antonjohansson.vaadin.recaptcha.options.RecaptchaSize.NORMAL;
import static com.antonjohansson.vaadin.recaptcha.options.RecaptchaTheme.LIGHT;
import static com.antonjohansson.vaadin.recaptcha.options.RecaptchaType.IMAGE;
import static com.vaadin.server.Sizeable.Unit.EM;
import static com.vaadin.ui.Alignment.MIDDLE_CENTER;

import java.util.Optional;

import com.antonjohansson.vaadin.recaptcha.Recaptcha;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * The view of the service.
 */
class ServiceView extends VerticalLayout
{
    private static final float STANDARD_WIDTH = 18.9F;

    private final Layout requestTokenLayout;
    private final Layout useTokenLayout;
    private final Layout setPasswordLayout;
    private Optional<Layout> currentLayout = Optional.empty();

    // Components
    final TextField username = new TextField();
    final Recaptcha captcha = new Recaptcha();
    final Button requestToken = new Button();
    final Label greeting = new Label();
    final TextField token = new TextField();
    final Button useToken = new Button();
    final Button backFromUseToken = new Button();
    final PasswordField newPassword = new PasswordField();
    final PasswordField newPasswordRepeat = new PasswordField();
    final Button setPassword = new Button();
    final Button backFromSetPassword = new Button();

    ServiceView()
    {
        requestTokenLayout = requestTokenLayout();
        useTokenLayout = useTokenLayout();
        setPasswordLayout = setPasswordLayout();

        addComponent(requestTokenLayout);
        addComponent(useTokenLayout);
        addComponent(setPasswordLayout);
        setSizeFull();
        setComponentAlignment(requestTokenLayout, MIDDLE_CENTER);
        setComponentAlignment(useTokenLayout, MIDDLE_CENTER);
        setComponentAlignment(setPasswordLayout, MIDDLE_CENTER);
    }

    private Layout requestTokenLayout()
    {
        username.setInputPrompt("Username");
        username.setWidth(STANDARD_WIDTH, EM);
        captcha.setSize(NORMAL);
        captcha.setTheme(LIGHT);
        captcha.setType(IMAGE);
        requestToken.setCaption("Request token");
        requestToken.setWidth(STANDARD_WIDTH, EM);
        requestToken.setEnabled(false);

        VerticalLayout layout = new VerticalLayout();
        layout.addComponents(username, captcha, requestToken);
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setVisible(false);
        layout.setWidthUndefined();

        return layout;
    }

    private Layout useTokenLayout()
    {
        token.setInputPrompt("Token");
        token.setWidth(STANDARD_WIDTH, EM);
        useToken.setCaption("Next");
        backFromUseToken.setCaption("Back");

        HorizontalLayout buttons = new HorizontalLayout(useToken, backFromUseToken);
        buttons.setSpacing(true);

        VerticalLayout layout = new VerticalLayout();
        layout.addComponents(greeting, token, buttons);
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setVisible(false);
        layout.setWidthUndefined();

        return layout;
    }

    private Layout setPasswordLayout()
    {
        newPassword.setWidth(STANDARD_WIDTH, EM);
        newPassword.setCaption("Password");
        newPasswordRepeat.setWidth(STANDARD_WIDTH, EM);
        newPasswordRepeat.setCaption("Repeat password");
        setPassword.setCaption("Set password");
        backFromSetPassword.setCaption("Back");

        HorizontalLayout buttons = new HorizontalLayout(setPassword, backFromSetPassword);
        buttons.setSpacing(true);

        VerticalLayout layout = new VerticalLayout();
        layout.addComponents(newPassword, newPasswordRepeat, buttons);
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setVisible(false);
        layout.setWidthUndefined();

        return layout;
    }

    /**
     * Clears the view from input.
     */
    void clear()
    {
        captcha.reset();
        requestToken.setEnabled(false);
        username.setValue("");
        greeting.setValue("");
        token.setValue("");
        newPassword.setValue("");
        newPasswordRepeat.setValue("");
        show(REQUEST_TOKEN);
    }

    /**
     * Shows a stage.
     *
     * @param stage The stage to show.
     */
    void show(Stage stage)
    {
        Layout layout = getLayout(stage);

        currentLayout.ifPresent(l -> l.setVisible(false));
        currentLayout = Optional.of(layout);
        layout.setVisible(true);
    }

    private Layout getLayout(Stage stage)
    {
        switch (stage)
        {
            case REQUEST_TOKEN:
                return requestTokenLayout;
            case USE_TOKEN:
                return useTokenLayout;
            case SET_PASSWORD:
                return setPasswordLayout;
            default:
                throw new IllegalArgumentException("stage");
        }
    }

    /**
     * Defines stages of the view.
     */
    enum Stage
    {
        REQUEST_TOKEN,
        USE_TOKEN,
        SET_PASSWORD
    }
}
