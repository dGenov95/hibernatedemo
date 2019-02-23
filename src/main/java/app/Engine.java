package app;

import app.entities.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Engine implements Runnable {

    private final EntityManager entityManager;

    public Engine(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void run() {
//        removeObjects();
//        containsEmployee();
//        employeesWithSalaryOverFiftyThousand();
//        employeesFromDepartment();
//        addingAddressAndUpdatingEmployee();
//        addressesWithEmployeeCount();
//        employeeWithProject();
//        findLatestTenProjects();
//        increaseSalaries();
//        removeTowns();
//        findEmployeesByFirstName();
        employeesMaximumSalaries();
    }



    /**
     * Problem 2. Remove Objects -
     * Use the soft_uni database. Persist all towns from the database. Detach those whose name length is more than 5
     * symbols. Then transform the names of all attached towns to lowercase and save them to the database.
     */
    private void removeObjects() {
        entityManager.getTransaction().begin();
        TypedQuery<Town> query = entityManager.createQuery("FROM Town", Town.class);
        List<Town> towns = query.getResultList();
        towns.stream()
                .filter(town -> town.getName().length() > 5)
                .forEach(entityManager::detach);
        towns.forEach(town -> {
            town.setName(town.getName().toLowerCase());
            if (entityManager.contains(town)) {
                entityManager.persist(town);
            }
        });
        entityManager.getTransaction().commit();
    }

    /**
     * Problem 3. Contains Employee
     * Use the soft_uni database. Write a program that checks if a given employee name is contained in the database.
     */
    private void containsEmployee() {
        Scanner scanner = new Scanner(System.in);
        String employeeName = scanner.nextLine();
        entityManager.getTransaction().begin();
        Query query = entityManager.createQuery("FROM Employee WHERE concat(first_name, ' ', last_name) = :name", Employee.class);
        query.setParameter("name", employeeName);
        try {
            query.getSingleResult();
            System.out.println("Yes");
        } catch (NoResultException nre) {
            System.out.println("No");
        }
        entityManager.getTransaction().commit();
    }

    /**
     * Problem 4. Employees with Salary Over 50 000
     * Write a program that gets the first name of all employees who have salary over 50 000.
     */
    private void employeesWithSalaryOverFiftyThousand() {
        entityManager.getTransaction().begin();
        List<Employee> employees = entityManager.createQuery("FROM Employee WHERE salary > 50000", Employee.class).getResultList();
        employees.forEach(employee -> System.out.println(employee.getFirstName()));
        entityManager.getTransaction().commit();
    }

    /**
     * Problem 5. Employees from Department
     * Extract all employees from the Research and Development department. Order them by salary (in ascending order),
     * then by id (in asc order). Print only their first name, last name, department name and salary.
     */
    private void employeesFromDepartment() {
        entityManager.getTransaction().begin();
        String queryString = "SELECT e FROM Employee e WHERE e.department.name = :departmentName ORDER BY e.salary, e.id";
        TypedQuery<Employee> query = entityManager.createQuery(queryString, Employee.class);
        query.setParameter("departmentName", "Research and Development");
        List<Employee> employees = query.getResultList();
        employees.forEach(employee -> System.out.printf("%s %s from %s - $%.2f%n",
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDepartment().getName(),
                employee.getSalary()));
        entityManager.getTransaction().commit();
    }

    private void addingAddressAndUpdatingEmployee() {
        Scanner scanner = new Scanner(System.in);
        String employeeLastName = scanner.nextLine();
        entityManager.getTransaction().begin();
        Address newAddress = new Address();
        newAddress.setText("Vitoshka 15");
        Town town = entityManager.createQuery("FROM Town WHERE name = 'Sofia'", Town.class).getSingleResult();
        newAddress.setTown(town);
        entityManager.persist(newAddress);
        Employee employee = entityManager.createQuery("FROM Employee WHERE last_name = :empLastName", Employee.class)
                .setParameter("empLastName", employeeLastName)
                .getSingleResult();
        entityManager.detach(employee.getAddress());
        employee.setAddress(newAddress);
        entityManager.merge(employee);
        entityManager.getTransaction().commit();
    }

    /**
     * Problem 7. Addresses with Employee Count
     * Find all addresses, ordered by the number of employees who live there (descending), then by town id (ascending).
     * Take only the first 10 addresses and print their address text, town name and employee count.
     */
    private void addressesWithEmployeeCount() {
        entityManager.getTransaction().begin();
        List<Address> addresses = entityManager.createQuery("FROM Address a ORDER BY a.employees.size DESC, a.town.id", Address.class)
                .setMaxResults(10)
                .getResultList();
        addresses.forEach(address -> System.out.printf("%s, %s - %d employees%n", address.getText(), address.getTown().getName(), address.getEmployees().size()));
    }

    /**
     * Problem 8. Get Employee with Project
     * Get an employee by his/her id. Print only his/her first name, last name, job title and projects (only their names).
     * The projects should be ordered by name (ascending). The output should be printed in the format given in the
     * example.
     */
    private void employeeWithProject() {
        Scanner scanner = new Scanner(System.in);
        int id = Integer.parseInt(scanner.nextLine());
        entityManager.getTransaction().begin();
        Employee employee = entityManager.createQuery("FROM Employee WHERE id = :id", Employee.class)
                .setParameter("id", id)
                .getSingleResult();
        StringBuilder sb = new StringBuilder();
        sb.append(employee.getFirstName()).append(" ").append(employee.getLastName()); //name
        sb.append(" - ");
        sb.append(employee.getJobTitle());
        sb.append("\n");
        sb.append(employee.getProjects().stream()
                .sorted(Comparator.comparing(Project::getName))
                .map(Project::getName)
                .collect(Collectors.joining("\n")));
        System.out.println(sb);
    }

    /**
     * Problem 9. Find Latest 10 Projects
     * Write a program that prints the last 10 started projects. Print their name, description, start and end date and sort
     * them by name lexicographically. For the output, check the format from the example.
     */
    private void findLatestTenProjects() {
        entityManager.getTransaction().begin();
        List<Project> projects = entityManager.createQuery("FROM Project ORDER BY start_date DESC", Project.class)
                .setMaxResults(10)
                .getResultList();
        projects.stream()
                .sorted(Comparator.comparing(Project::getName))
                .forEach(project -> System.out.printf("Project name: %s%n\tProject description: %s%n\tProject Start Date: %s%n\tProject End Date: %s%n",
                        project.getName(),
                        project.getDescription(),
                        project.getStartDate().toString(),
                        project.getEndDate() == null ? "null" : project.getEndDate().toString()));
    }

    /**
     * Problem 10. Increase Salaries
     * Write a program that increases the salaries of all employees, who are in the Engineering, Tool Design, Marketing or
     * Information Services departments by 12%. Then print the first name, the last name and the salary for the
     * employees, whose salary was increased.
     */
    private void increaseSalaries() {
        entityManager.getTransaction().begin();
        List<Employee> employees = entityManager.createQuery("FROM Employee e WHERE e.department.name IN(:engineering,:toolDesign,:marketing,:it)", Employee.class)
                .setParameter("engineering", "Engineering")
                .setParameter("toolDesign", "Tool Design")
                .setParameter("marketing", "Marketing")
                .setParameter("it", "Information Services")
                .getResultList();
        employees.forEach(employee -> employee.setSalary(employee.getSalary().multiply(BigDecimal.valueOf(1.12))));
        employees.forEach(employee -> System.out.printf("%s %s - %s ($%.2f)%n",
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDepartment().getName(),
                employee.getSalary().doubleValue()));
    }

    /**
     * Problem 11. Remove Towns
     * Write a program that deletes a town, which name is given as an input. The program should delete all addresses that
     * are in the given town. Print on the console the number of addresses that were deleted. Check the example for the
     * output format.
     */
    private void removeTowns() {
        Scanner scanner = new Scanner(System.in);
        String townName = scanner.nextLine();
        Town town = entityManager.createQuery("FROM Town t WHERE t.name = :townName", Town.class)
                .setParameter("townName", townName)
                .getSingleResult();
        List<Address> townAddresses = entityManager.createQuery("FROM Address a WHERE a.town.id = :townId", Address.class)
                .setParameter("townId", town.getId())
                .getResultList();
        System.out.printf("%d in %s deleted%n", townAddresses.size(), town.getName());

        entityManager.getTransaction().begin();

        townAddresses.forEach(townAddress -> {
            townAddress.getEmployees().forEach(employee -> employee.setAddress(null));
            entityManager.remove(townAddress);
        });
        entityManager.getTransaction().commit();
    }

    /**
     * Problem 12. Find Employees by First Name
     * Write a program that finds all employees, whose first name starts with a pattern given as an input from the
     * console. Print their first and last names, their job title and salary in the format given in the example below.
     */
    private void findEmployeesByFirstName(){
        Scanner scanner = new Scanner(System.in);

        String namePattern = scanner.nextLine();

        entityManager.getTransaction().begin();

        List<Employee> employees = entityManager.createQuery("FROM Employee WHERE first_name LIKE :pattern", Employee.class)
                .setParameter("pattern", namePattern + "%")
                .getResultList();
        employees.forEach(employee -> System.out.printf("%s %s - %s - ($%.2f)%n",
                employee.getFirstName(),
                employee.getLastName(),
                employee.getJobTitle(),
                employee.getSalary().doubleValue()));
        entityManager.getTransaction().commit();
    }

    /**
     * Problem 13. Employees Maximum Salaries
     * Write a program that finds the max salary for each department. Filter the departments, which max salaries are not
     * in the range between 30000 and 70000.
     */
    private void employeesMaximumSalaries(){
        entityManager.getTransaction().begin();
        entityManager.createQuery("FROM Employee e GROUP BY e.department.id HAVING e.salary NOT BETWEEN 30000 AND 70000", Employee.class)
                .getResultList()
                .forEach(employee -> System.out.printf("%s - %.2f%n",
                        employee.getDepartment().getName(),
                        employee.getSalary().doubleValue()));

    }

}
