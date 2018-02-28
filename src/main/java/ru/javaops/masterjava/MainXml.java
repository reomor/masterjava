package ru.javaops.masterjava;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.Project;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;

import javax.xml.validation.Schema;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MainXml {
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

    private static <T extends User> void printSet(Set<T> set) {
        System.out.println("===");
        for (T element : set) {
            System.out.println(element.getEmail());
        }
        System.out.println("===");
    }

    public static void main(String[] args) throws Exception {
        final URL resourceUrl = Resources.getResource("payload.xml");
        final Schema resourceSchema = Schemas.ofClasspath("payload.xsd");

        findUsersByProjectJaxb("topjava", resourceUrl, resourceSchema);
        findUsersByProjectJaxb("basejava", resourceUrl, resourceSchema);
        findUsersByProjectJaxb("masterjava", resourceUrl, resourceSchema);
    }
}
