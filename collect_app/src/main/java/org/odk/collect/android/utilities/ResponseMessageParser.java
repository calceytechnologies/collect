package org.odk.collect.android.utilities;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import timber.log.Timber;

public class ResponseMessageParser {

    private static final String MESSAGE_XML_TAG = "message";
    private static final String META_DATA_XML_TAG = "submissionMetadata";
    private static final String ATTR_INSTANCE_ID_TAG = "instanceID";

    private boolean isValid;
    private String messageResponse;
    private String submissionInstanceId;

    public boolean isValid() {
        return this.isValid;
    }

    public String getMessageResponse() {
        return messageResponse;
    }

    public String getSubmissionInstanceId() {
        return submissionInstanceId;
    }

    public void setMessageResponse(String response) {
        isValid = false;
        try {
            if (response.contains("OpenRosaResponse")) {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(response.getBytes()));
                doc.getDocumentElement().normalize();

                if (doc.getElementsByTagName(MESSAGE_XML_TAG).item(0) != null) {
                    messageResponse = doc.getElementsByTagName(MESSAGE_XML_TAG).item(0).getTextContent();
                    isValid = true;

                    setSubmissionInstanceId(response);
                }
            }

        } catch (SAXException | IOException | ParserConfigurationException e) {
            Timber.e(e, "Error parsing XML message due to %s ", e.getMessage());
        }
    }

    private void setSubmissionInstanceId(String response) {
        try {
            if (response.contains("OpenRosaResponse")) {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(response.getBytes()));
                doc.getDocumentElement().normalize();

                NodeList nodeList = doc.getElementsByTagName(META_DATA_XML_TAG);

                for (int i = 0; i < nodeList.getLength(); ++i) {
                    String nodeValue = nodeList.item(i).getAttributes()
                            .getNamedItem(ATTR_INSTANCE_ID_TAG).getNodeValue();

                    if(nodeValue != null){
                        submissionInstanceId = nodeValue;
                    }
                }
            }

        } catch (SAXException | IOException | ParserConfigurationException e) {
            Timber.e(e, "Error parsing XML message due to %s ", e.getMessage());
        }
    }

}
