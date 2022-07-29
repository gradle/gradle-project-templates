/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradlex.templates;

import org.gradle.api.internal.tasks.userinput.UserInputHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interrogator {

    private UserInputHandler userInputHandler;

    public Interrogator(UserInputHandler userInputHandler) {
        this.userInputHandler = userInputHandler;
    }

    public Map<String, Object> askQuestions(List<Question> questions) {
        Map<String, Object> answers = new HashMap<>();
        for (Question question : questions) {
            // TODO: add check if the entry already exists?
            answers.put(question.getName(), question.ask(userInputHandler));
        }
        return answers;
    }

}
