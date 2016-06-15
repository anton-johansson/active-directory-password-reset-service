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
package com.antonjohansson.lprs.controller.token;

import com.antonjohansson.lprs.model.User;

/**
 * {@link ITokenSender} implementation that prints the token to the console.
 */
class ConsoleTokenSender implements ITokenSender
{
    @Override
    public void send(User user, String token)
    {
        System.out.println("Generated token '" + token + "'.");
    }
}
