/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.core.flow.iteration.controller;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.flow.iteration.LoopController;
import static it.greenvulcano.util.LambdaExceptionUtil.*;

/**
 * A {@link LoopController} implementation that handle well-formed XML iterating over root childs.
 *
 * The results are returned in a {@link Document} using the same node names found in the input object
 * 
 * @version 4.0.0 20160603
 * @author GreenVulcano Developer Team
 * 
 */
public class XmlLoopCotroller extends BaseLoopController {

	private GVBuffer inputBuffer;

	private final String CHILD_NODE_NAME ="CHILD_NODE_NAME";
	
	private String parentNode = "GVIterationResults";
	
	@Override
	protected GVBuffer doLoop(GVBuffer inputCollection) throws GVException, InterruptedException {
		inputBuffer = inputCollection;
		NodeList nodeList =  parseGVBuffer(inputCollection).orElseThrow(() -> {
			return new GVException("GVCORE_UNPARSABLE_XML_DATA", new String[][]{{"name", "'collection-type'"},
                {"object", "" + inputCollection.getObject()}});
		});
		
		GVBuffer outputData = new GVBuffer(inputCollection, false);
		
		Document response = IntStream.range(0, nodeList.getLength())
									 .mapToObj(nodeList::item)
									 .map(this::buildLoopGVBuffer)
									 .filter(Optional::isPresent)
									 .map(Optional::get)
									 .map(rethrowFunction(this::performAction))
									 .reduce(buildResultsDocument(), this::bindResult, this::mergeResults);
		
		outputData.setObject(response);
		
		return outputData;
	}
	
	private Optional<NodeList> parseGVBuffer(GVBuffer data){
		NodeList nodeList = null;
		try {
			if (data.getObject() instanceof NodeList) {
				nodeList = (NodeList)data.getObject();
			} else if (data.getObject() instanceof Document) {
				Document document = (Document) data.getObject();
				parentNode = document.getDocumentElement().getTagName();
				nodeList = document.getDocumentElement().getChildNodes();
			} else if (data.getObject() instanceof Node) {
				Node node = (Node) data.getObject();
				parentNode = node.getNodeName();
				nodeList = node.getChildNodes();
			} else  {
				DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource xmlInputSource = new InputSource();
				
				String xmlSource = (data.getObject() instanceof byte[]) ?
				                   new String((byte[]) data.getObject(), StandardCharsets.UTF_8):
				                   data.getObject().toString();    
				
				xmlInputSource.setCharacterStream(new StringReader(xmlSource));
				
				Document document = documentBuilder.parse(xmlInputSource);
				parentNode = document.getDocumentElement().getTagName();
				nodeList = document.getDocumentElement().getChildNodes();
				
			} 
		} catch (Exception e) {
			LOG.error("Invalid XML data "+data.getObject(), e);
		}
		
		return Optional.ofNullable(nodeList);
	}
	
	private Optional<GVBuffer> buildLoopGVBuffer(Node node) {
		
		GVBuffer itemData = null;
		try {
			itemData = new GVBuffer(inputBuffer, false);			
			itemData.setProperty(CHILD_NODE_NAME, node.getNodeName());
			itemData.setObject(node);
		} catch (Exception e) {
			LOG.error("Exception on GVBuffer creation ", e);
		}
		return Optional.ofNullable(itemData);		
	}
	
	private Document buildResultsDocument() {
		Document document = null;
		try {
			DocumentBuilder documentBuilder;
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = documentBuilder.newDocument();
			document.appendChild(document.createElement(parentNode));
		} catch (ParserConfigurationException e) {
			LOG.error("Error creating response document ", e);		
		}
		
		return document;
	}
	
	private Document bindResult(Document document, GVBuffer data) {
		Node node = null;
		
		try {
			if (data.getObject() instanceof Node) {
				node = (Node) data.getObject();
			} else if (data.getObject() instanceof  Document) {
				node = Document.class.cast(data.getObject()).getDocumentElement();
			} else if (data.getObject() instanceof String) {
				DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource xmlInputSource = new InputSource();
				xmlInputSource.setCharacterStream(new StringReader(data.getObject().toString()));
				
				node = documentBuilder.parse(xmlInputSource).getDocumentElement();
			}
		} catch (Exception e){
			LOG.error("Invalid XML data "+data.getObject(), e);
		}
		
		if (Objects.isNull(node)) {
			String nodeName = data.getProperty(CHILD_NODE_NAME);
			node = document.createElement(nodeName);
			if (Objects.nonNull(data.getObject())) {
				node.appendChild(document.createCDATASection(data.getObject().toString()));
			}
		}
		
	    document.getDocumentElement().appendChild(document.importNode(node, true));
		
		return document;
	}
	
	private Document mergeResults(Document result, Document otherResult){ 
				
		NodeList nodeList = result.importNode(otherResult.getDocumentElement(), true).getChildNodes();
		
		IntStream.range(0, nodeList.getLength())
					.mapToObj(nodeList::item)
					.forEach(result.getDocumentElement()::appendChild);
	
		return result;
	}

    @Override
    public void cleanup() {
    	inputBuffer = null;
    }

}
