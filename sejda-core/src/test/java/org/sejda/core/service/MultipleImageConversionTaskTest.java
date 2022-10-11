/*
 * Created on 08/mar/2013
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
package org.sejda.core.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.sejda.TestUtils.encryptedAtRest;
import static org.sejda.TestUtils.getEncryptionAtRestPolicy;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Ignore;
import org.junit.Test;
import org.sejda.model.image.ImageColorType;
import org.sejda.model.parameter.image.AbstractPdfToMultipleImageParameters;
import org.sejda.model.pdf.page.PageRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Vacondio
 * 
 */
@Ignore
public abstract class MultipleImageConversionTaskTest<T extends AbstractPdfToMultipleImageParameters>
        extends BaseTaskTest<T> implements TestableTask<T> {
    private static Logger LOG = LoggerFactory.getLogger(MultipleImageConversionTaskTest.class);

    abstract T getMultipleImageParametersWithoutSource(ImageColorType type);

    @Test
    public void testExecuteEncryptedStreamToMultipleImage() throws IOException {
        AbstractPdfToMultipleImageParameters parameters = getMultipleImageParametersWithoutSource(
                ImageColorType.GRAY_SCALE);
        parameters.addSource(encryptedInput());
        doExecute(parameters, 4);
    }

    @Test
    public void testExecuteStreamToMultipleImage() throws IOException {
        AbstractPdfToMultipleImageParameters parameters = getMultipleImageParametersWithoutSource(
                ImageColorType.GRAY_SCALE);
        parameters.addSource(customInput("pdf/test_jpg.pdf"));
        doExecute(parameters, 1);
    }

    @Test
    public void testExecuteStreamToMultipleImageWithPageSelection() throws IOException {
        AbstractPdfToMultipleImageParameters parameters = getMultipleImageParametersWithoutSource(
                ImageColorType.GRAY_SCALE);
        parameters.addSource(shortInput());
        parameters.addPageRange(new PageRange(2, 3));
        doExecute(parameters, 2);
    }

    @Test
    public void testMultipleInputs() throws IOException {
        AbstractPdfToMultipleImageParameters parameters = getMultipleImageParametersWithoutSource(
                ImageColorType.GRAY_SCALE);
        parameters.addSource(mediumInput());
        parameters.addSource(regularInput());
        parameters.addPageRange(new PageRange(1, 1));
        parameters.setOutputPrefix("[BASENAME]-[PAGENUMBER]");
        doExecute(parameters, 2);
    }

    @Test
    public void encryptionAtRestTest() throws IOException {
        AbstractPdfToMultipleImageParameters parameters = getMultipleImageParametersWithoutSource(
                ImageColorType.GRAY_SCALE);
        parameters.addSource(encryptedAtRest(mediumInput()));
        parameters.addSource(encryptedAtRest(regularInput()));
        parameters.addPageRange(new PageRange(1, 1));
        parameters.setOutputPrefix("[BASENAME]-[PAGENUMBER]");
        doExecute(parameters, 2);
    }

    @Test
    public void specificResultFilenames() throws IOException {
        AbstractPdfToMultipleImageParameters parameters = getMultipleImageParametersWithoutSource(
                ImageColorType.GRAY_SCALE);
        parameters.addSource(encryptedInput());
        parameters.setOutputPrefix("[FILENUMBER]_[BASENAME]");
        testContext.directoryOutputTo(parameters);
        parameters.addSpecificResultFilename("one");
        parameters.addSpecificResultFilename("two");
        String ext = parameters.getOutputImageType().getExtension();
        execute(parameters);
        testContext.assertTaskCompleted();
        testContext.assertOutputSize(4).assertOutputContainsFilenames("one." + ext, "two." + ext,
                "3_encrypted-test-file." + ext, "4_encrypted-test-file." + ext);
    }

    @Test
    public void encryptionAtRestRoundTrip() throws IOException {
        AbstractPdfToMultipleImageParameters parameters = getMultipleImageParametersWithoutSource(
                ImageColorType.COLOR_RGB);
        parameters.addSource(encryptedAtRest(shortInput()));
        parameters.addSource(encryptedAtRest(mediumInput()));
        parameters.addPageRange(new PageRange(1, 1));

        testContext.directoryOutputTo(parameters);
        parameters.getOutput().setEncryptionAtRestPolicy(getEncryptionAtRestPolicy());

        execute(parameters);

        testContext.assertTaskCompleted();
        testContext.assertNoTaskWarnings();
        testContext.assertOutputSize(2).forEachRawOutput(path -> {
            try {
                BufferedImage image = ImageIO
                        .read(getEncryptionAtRestPolicy().decrypt(new FileInputStream(path.toFile())));

                assertNotNull(image);
                assertTrue(image.getHeight() > 0);
                assertTrue(image.getWidth() > 0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    void doExecute(AbstractPdfToMultipleImageParameters parameters, int size) throws IOException {
        testContext.directoryOutputTo(parameters);
        execute(parameters);
        testContext.assertTaskCompleted();
        testContext.assertOutputSize(size).forEachRawOutput(p -> {
            try {
                RenderedImage ri = ImageIO.read(p.toFile());
                assertTrue(ri.getHeight() > 0);
                assertTrue(ri.getWidth() > 0);
            } catch (Exception e) {
                LOG.error("Test failed", e);
                fail(e.getMessage());
            }
        });
    }
}
