package ru.javaops.masterjava;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.Project;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;

import java.util.*;
import java.util.stream.Collectors;

public class MainXml {
    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    public static Set<User> findUsersByProjectName(String projectName) throws Exception {
        Payload payload = JAXB_PARSER.unmarshal(Resources.getResource("payload.xml").openStream());

        Project project = payload.getProjects().getProject().stream()
                .filter(proj -> proj.getName().equals(projectName))
                .findAny()
                .orElse(new Project());

        final Set<String> projectGroupNames = project.getGroup().stream()
                .map(Project.Group::getName)
                .collect(Collectors.toSet());

        Set<User> projectUsers = new TreeSet<>(Comparator.comparing(User::getEmail));

        for (User user : payload.getUsers().getUser()) {
            for (Object objectGroup : user.getGroups()) {
                Project.Group group = (Project.Group) objectGroup;
                if (projectGroupNames.contains(group.getName())) {
                    projectUsers.add(user);
                    break;
                }
            }
        }

        return projectUsers;
    }

    private static  <T extends User> void printSet(Set<T> set) {
        System.out.println("===");
        for (T element : set) {
            System.out.println(element.getEmail());
        }
        System.out.println("===");
    }

    public static void main(String[] args) throws Exception {
        final Set<User> usersTopjava = findUsersByProjectName("topjava");
        printSet(usersTopjava);
        final Set<User> usersBasejava = findUsersByProjectName("basejava");
        printSet(usersBasejava);
        final Set<User> usersMasterjava = findUsersByProjectName("masterjava");
        printSet(usersMasterjava);
    }
}
