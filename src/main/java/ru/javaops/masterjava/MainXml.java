package ru.javaops.masterjava;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.Project;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.validation.Schema;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class MainXml {

    public static void main(String[] args) throws Exception {
        final URL resourceUrl = Resources.getResource("payload.xml");
        final Schema resourceSchema = Schemas.ofClasspath("payload.xsd");
/*
        findUsersByProjectJaxb("topjava", resourceUrl, resourceSchema);
        findUsersByProjectJaxb("basejava", resourceUrl, resourceSchema);
        findUsersByProjectJaxb("masterjava", resourceUrl, resourceSchema);
//*/
        findUsersByProjectStax("masterjava", resourceUrl);
        findUsersByProjectStax("basejava", resourceUrl);
        findUsersByProjectStax("topjava", resourceUrl);
    }

    public static void findUsersByProjectJaxb(String projectName, URL resourceUrl, Schema resourceSchema) throws Exception {

        final JaxbParser parserJaxb = new JaxbParser(ObjectFactory.class);
        parserJaxb.setSchema(resourceSchema);

        Set<User> projectUsers = new TreeSet<>(Comparator.comparing(User::getEmail));

        try (InputStream inputStream = resourceUrl.openStream()) {
            Payload payload = parserJaxb.unmarshal(inputStream);
            Project project = payload.getProjects().getProject().stream()
                    .filter(proj -> proj.getName().equals(projectName))
                    .findAny()
                    .orElse(new Project());

            final Set<String> projectGroupNames = project.getGroup().stream()
                    .map(Project.Group::getName)
                    .collect(Collectors.toSet());

            for (User user : payload.getUsers().getUser()) {
                for (Object objectGroup : user.getGroups()) {
                    Project.Group group = (Project.Group) objectGroup;
                    if (projectGroupNames.contains(group.getName())) {
                        projectUsers.add(user);
                        break;
                    }
                }
            }
        }

        printSet(projectUsers);
    }

    private static String PROJECTS_TAG = "Projects";
    private static String PROJECT_TAG = "Project";
    private static String USERS_TAG = "Users";
    private static String USER_TAG = "User";

    public static void findUsersByProjectStax(String projectName, URL resourceUrl) throws Exception {
        Set<User> projectUsers = new TreeSet<>(Comparator.comparing(User::getEmail));

        try(StaxStreamProcessor processor = new StaxStreamProcessor(resourceUrl.openStream())) {
            XMLStreamReader reader = processor.getReader();
            Set<String> groupNames = new HashSet<>();

            processor.doUntil(XMLEvent.START_ELEMENT, PROJECTS_TAG);
            while(processor.doUntilFindTag("Project", PROJECTS_TAG, USERS_TAG)) {
                if (processor.getAttributeValue("name").equals(projectName)) {
                    while (processor.doUntilFindTag("Group", PROJECT_TAG, PROJECTS_TAG, USERS_TAG)) {
                        groupNames.add(processor.getAttributeValue("name"));
                    }
                }
            }
            //System.out.println(reader.getLocalName());
            //go to users, stop if user
            processor.doUntilFindTag(USERS_TAG, USER_TAG);

            while (processor.doUntilFindTag("User", USERS_TAG)) {
                String[] groups = processor.getAttributeValue("groups").split(" ");
                for (String group : groups) {
                    if (groupNames.contains(group)) {
                        User user = new User();
                        user.setEmail(processor.getAttributeValue("email"));
                        user.setValue(reader.getElementText());
                        projectUsers.add(user);
                    }
                }
            }
            System.out.println(Arrays.toString(groupNames.toArray()));
        }

        printSet(projectUsers);

        final String html = body(
                h1("table " + projectName),
                table(
                        each(projectUsers, user -> tr(
                                td(user.getValue()),
                                td(user.getEmail())
                        ))
                )
        ).render();

        System.out.println(html);
    }

    private static <T extends User> void printSet(Set<T> set) {
        System.out.println("===");
        for (T element : set) {
            System.out.println("name: " + element.getValue() + " email: " + element.getEmail());
        }
        System.out.println("===");
    }
}
