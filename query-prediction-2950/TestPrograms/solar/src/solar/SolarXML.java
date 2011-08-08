package solar;

import java.io.*;
import java.util.Iterator;
import org.dom4j.*;
import org.dom4j.io.*;

/**
 * A static class handling all XML input/output for a Solar simulation.
 * @author jardini
 */
public final class SolarXML {

    private SolarXML() {
    }

    /**
     * Parses the given input file, placing the gathered information in
     * timeInfo and solarBodyManager
     * @param inFile - An input XML file
     * @param timeInfo - A non-null SolarTimeInfo to keep track of time constants
     * @param solarBodyManager - A non-null SolarBodyManager to track SolarBodies
     * @throws SolarXMLInputException
     */
    public static void parseInput(String inFile, SolarTimeInfo timeInfo, SolarBodyManager solarBodyManager) throws SolarXMLInputException {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new FileReader(inFile));
            Element root = document.getRootElement();
            //Makes sure root name is SOLAR
            if (!root.getName().equals("SOLAR")) {
                throw new SolarXMLInputException("Invalid root tag: should be SOLAR");
            }
            parseRootAttributes(root, timeInfo);
            // Parses through each element of the root
            for (Iterator i = root.elementIterator(); i.hasNext();) {
                solarBodyManager.addBody(xmlToSolarBody((Element) i.next(), solarBodyManager));
            }
        } catch (NumberFormatException e) {
            // Catches any invalid/improperly formatted XML inFile
            System.err.println("Invalid number in XML input");
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            System.exit(1);
        } catch (DocumentException e) {
            System.err.println("Invalid formatting in XML input");
            System.exit(1);
        }
    }

    /**
     * Helper method to split up parsing for XML input
     * @param root - The SOLAR element
     * @param timeInfo - The time container
     * @throws SolarXMLInputException
     */
    private static void parseRootAttributes(Element root, SolarTimeInfo timeInfo) throws SolarXMLInputException {
        double init = -1, step = -1, numSteps = -1;
        // Parses the root element's attributes
        for (Iterator i = root.attributeIterator(); i.hasNext();) {
            Attribute attr = (Attribute) i.next();
            String attrName = attr.getName();
            if (attrName.equals("TIME")) {
                init = Double.parseDouble(attr.getValue());
            } else if (attrName.equals("TIMESTEP")) {
                step = Double.parseDouble(attr.getValue());
            } else if (attrName.equals("TIMESTEPS_TO_RUN")) {
                numSteps = Double.parseDouble(attr.getValue());
            } else {
                throw new SolarXMLInputException("Invalid attribute in SOLAR tag");
            }
            // Iterates through each SolarBody
        }
        if (init >= 0 && step >= 0 && numSteps >= 0) {
            timeInfo.setInitialTime(init);
            timeInfo.setTimestep(step);
            timeInfo.setTimestepsToRun(numSteps);
        } else {
            throw new SolarXMLInputException("Invalid/missing time value in SOLAR tag");
        }
    }

    /**
     * Writes the current state of a simulation into the file outFile.
     * @param outFile - A String representing a file path.
     * @param timeInfo - A non-null SolarTimeInfo
     * @param currTime - The current simulation time.
     * @param solarBodyManager - A non-null SolarBodyManager.
     */
    public static void writeOutput(String outFile, SolarTimeInfo timeInfo, SolarBodyManager solarBodyManager, double currTime) {
        try {
            Document document = DocumentHelper.createDocument();
            OutputFormat formatPretty = OutputFormat.createPrettyPrint();
            XMLWriter fileWriter = new XMLWriter(new FileWriter(outFile), formatPretty);

            Element root = DocumentHelper.createElement("SOLAR");
            document.setRootElement(root);
            root.addAttribute("TIME", String.valueOf(currTime));
            root.addAttribute("TIMESTEP", String.valueOf(timeInfo.getTimestep()));
            root.addAttribute("TIMESTEPS_TO_RUN", String.valueOf(timeInfo.getTimestepsToRun()));

            for (SolarBody body : solarBodyManager.getBodies()) {
                root.add(solarBodyToXML(body));
            }
            fileWriter.write(document);
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("Error writing to xml file");
            System.exit(1);
        }
    }

    /**
     * Converts a SolarBody to an XML representation.
     * @param body - Any SolarBody
     * @return A org.dom4j.Element representing this SolarBody.
     */
    private static Element solarBodyToXML(SolarBody body) {
        String type = body.getType();
        Element bodyXML = DocumentHelper.createElement("OBJECT");
        bodyXML.addAttribute("NAME", body.getName());
        bodyXML.addAttribute("MASS", String.valueOf(body.getMass()));
        bodyXML.addAttribute("RADIUS", String.valueOf(body.getRadius()));
        bodyXML.addAttribute("TYPE", type);

        Element posXML = DocumentHelper.createElement("POSITION");
        posXML.addAttribute("X", String.valueOf(body.getX()));
        posXML.addAttribute("Y", String.valueOf(body.getY()));
        posXML.addAttribute("Z", String.valueOf(body.getZ()));
        bodyXML.add(posXML);

        Element velXML = DocumentHelper.createElement("VELOCITY");
        SolarVector velocity = body.getVelocity();
        velXML.addAttribute("X", String.valueOf(velocity.getX()));
        velXML.addAttribute("Y", String.valueOf(velocity.getY()));
        velXML.addAttribute("Z", String.valueOf(velocity.getZ()));
        bodyXML.add(velXML);

        if (type.equals("ROCKET")) {
            Element thrustXML = DocumentHelper.createElement("THRUST");
            SolarVector thrust = body.getDefaultForce();
            thrustXML.addAttribute("X", String.valueOf(thrust.getX()));
            thrustXML.addAttribute("Y", String.valueOf(thrust.getY()));
            thrustXML.addAttribute("Z", String.valueOf(thrust.getZ()));
            bodyXML.add(thrustXML);
        }

        return bodyXML;
    }

    private static SolarBody xmlAttributesToSolarBody(Element root, SolarBodyManager solarBodyManager) throws SolarXMLInputException {
        String name = null, type = null;
        double mass = -1, radius = -1;
        // Loops through attributes of the object
        for (Iterator i = root.attributeIterator(); i.hasNext();) {
            Attribute attr = (Attribute) i.next();
            String attrName = attr.getName();
            if (attrName.equals("NAME")) {
                name = attr.getValue();
            } else if (attrName.equals("MASS")) {
                mass = Double.parseDouble(attr.getValue());
            } else if (attrName.equals("RADIUS")) {
                radius = Double.parseDouble(attr.getValue());
            } else if (attrName.equals("TYPE")) {
                type = attr.getValue();
            } else {
                throw new SolarXMLInputException("Invalid OBJECT attribute");
            }
        }
        if (name == null || type == null || mass <= 0 || radius < 0) {
            throw new SolarXMLInputException("Missing/Invalid OBJECT attribute");
        }
        return solarBodyManager.createBody(name, mass, radius, type);
    }

    /**
     * Converts an xml Element to a SolarBody
     * @param root - An xml Element representing a SolarBody
     * @param solarBodyManager - The manager for the simulation.
     * @return A new SolarBody (not yet added to the simulation)
     * @throws SolarXMLInputException
     */
    private static SolarBody xmlToSolarBody(Element root, SolarBodyManager solarBodyManager) throws SolarXMLInputException {
        SolarBody newBody = xmlAttributesToSolarBody(root, solarBodyManager);
        // Iterate through elements of that object
        for (Iterator i = root.elementIterator(); i.hasNext();) {
            Element element = (Element) i.next();
            String elementName = element.getName();
            double x = Double.NaN, y = Double.NaN, z = Double.NaN;
            // Iterate through the x, y, and z attributes of that element
            for (Iterator i2 = element.attributeIterator(); i2.hasNext();) {

                Attribute attr = (Attribute) i2.next();
                String attrName = attr.getName();
                if (attrName.equals("X")) {
                    x = Double.parseDouble(attr.getValue());
                } else if (attrName.equals("Y")) {
                    y = Double.parseDouble(attr.getValue());
                } else if (attrName.equals("Z")) {
                    z = Double.parseDouble(attr.getValue());
                } else {
                    throw new SolarXMLInputException("Invalid attribute: should be X, Y, or Z");
                }
            }
            // Checks that x, y, and z was input
            if (x != Double.NaN && y != Double.NaN && z != Double.NaN) {
                // Depending on the element, set the appropriate field
                if (elementName.equals("POSITION")) {
                    newBody.setPosition(x, y, z);
                } else if (elementName.equals("VELOCITY")) {
                    newBody.setVelocity(x, y, z);
                } else if (elementName.equals("THRUST")) {
                    newBody.setDefaultForce(x, y, z);
                } else {
                    throw new SolarXMLInputException("Invalid element in an OBJECT");
                }
            } else {
                throw new SolarXMLInputException("XML file missing a X, Y, or Z value");
            }
        }
        return newBody;
    }
}
