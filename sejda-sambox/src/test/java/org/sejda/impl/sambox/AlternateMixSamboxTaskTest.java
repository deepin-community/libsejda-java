/*
 * Created on 15/set/2011
 * Copyright 2011 by Andrea Vacondio (andrea.vacondio@gmail.com).
 * 
 * This file is part of the Sejda source code
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sejda.impl.sambox;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.sejda.core.service.AlternateMixTaskTest;
import org.sejda.impl.sambox.component.PdfTextExtractorByArea;
import org.sejda.model.exception.TaskIOException;
import org.sejda.model.parameter.AlternateMixMultipleInputParameters;
import org.sejda.model.task.Task;
import org.sejda.sambox.pdmodel.PDPage;

/**
 * @author Andrea Vacondio
 * 
 */
public class AlternateMixSamboxTaskTest extends AlternateMixTaskTest {

    @Override
    public Task<AlternateMixMultipleInputParameters> getTask() {
        return new AlternateMixTask();
    }

    @Override
    protected void assertHeaderContains(PDPage page, String expectedText) {
        try {
            assertThat(new PdfTextExtractorByArea().extractHeaderText(page).trim(), containsString(expectedText));
        } catch (TaskIOException e) {
            fail(e.getMessage());
        }
    }

}
