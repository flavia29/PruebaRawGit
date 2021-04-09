package es.dawequipo3.growing.controller;

import es.dawequipo3.growing.model.Category;
import es.dawequipo3.growing.model.Plan;
import es.dawequipo3.growing.model.User;
import es.dawequipo3.growing.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;


@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @Autowired
    private PlanService planService;

    @Autowired
    private TreeService treeService;

    @Autowired
    private UserService userService;


    @Autowired
    private CompletedPlanService completedPlanService;

    /**
     * This method marks a task of the category as done. It will save an instance to calculate the charts
     *
     * @param name    the plan's name to find the objects related to it like category or tree
     * @param request
     * @return categories.html
     */

    @PostMapping("/complete/{name}")
    public String updateTree(@PathVariable String name, HttpServletRequest request) {
        Plan plan = planService.findPlanByName(name).orElseThrow();
        request.getUserPrincipal().getName();
        User user = userService.findUserByEmail(request.getUserPrincipal().getName()).orElseThrow();
        planService.saveCompletedPlan(user, plan);
        String categoryName = plan.getCategory().getName();
        return "redirect:/categoryInfo/" + categoryName;
    }

    /**
     * This method has the finality of removing tasks done by users from the admin table located in Profile.html
     *
     * @param email the user on the table
     * @param plan  the plan to remove of the user
     * @param date  when it was completed
     * @return {@link UserController /profile }
     */
    @PostMapping("/removeCompletedPlan/{email}/{plan}/{date}")
    public String removeCompletedPlan(@PathVariable String email, @PathVariable String plan, @PathVariable String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS");
        try {
            Date dateObject = format.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateObject);
            long milisecs = calendar.getTimeInMillis();
            completedPlanService.deleteCompletedPlan(email, plan, milisecs);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "redirect:/profile";
    }

    /**
     * This method returns the list of categories the webapp has
     *
     * @param model
     * @param request the user logged
     * @return Categories.html
     */
    @GetMapping("/categories")
    public String categories(Model model, HttpServletRequest request) {
        model.addAttribute("error", request.isRequestedSessionIdFromCookie());
        model.addAttribute("registered", request.isUserInRole("USER"));
        model.addAttribute("admin", request.isUserInRole("ADMIN"));
        model.addAttribute("category", categoryService.findAll());
        return "categories";
    }

    /**
     * This one puts in the model the user information on the category referenced with {name}.
     * If is anonymous, it returns only the category name and its plans. When the user is logged, the web searches if
     * the plans will share are liked by the user or not
     *
     * @param model
     * @param name    the category name
     * @param request the user logged
     * @return CategoryInfo.html
     */
    @GetMapping("/categoryInfo/{name}")
    public String categoryInfo(Model model, @PathVariable String name, HttpServletRequest request) {
        model.addAttribute("registered", request.isUserInRole("USER"));
        model.addAttribute("admin", request.isUserInRole("ADMIN"));
        Category category = categoryService.findByName(name).orElseThrow();
        if (request.isUserInRole("USER")) {
            String email = request.getUserPrincipal().getName();
            User user = userService.findUserByEmail(email).orElseThrow();
            model.addAttribute("date", treeService.findTree(email, category.getName()).orElseThrow().getDate());
            model.addAttribute("image", treeService.findTree(email, category.getName()).orElseThrow().getImagePath());
            for (Plan plan : category.getPlans()) {
                plan.setLikedUser(planService.existsLiked(plan.getName(), user.getEmail()));
            }
            category.setLikedByUser(user.getUserFavoritesCategory().contains(category));
        } else {
            model.addAttribute("image", "/assets/img/progress/fase5.png");
        }

        model.addAttribute("category", category);

        return "categoryInfo";
    }

    /**
     * This method saves an instance of the user and the actual category to help the algorithm
     *
     * @param name    the category name
     * @param request
     * @return
     */

    @PostMapping("/categoryInfo/{name}/like")
    public String categoryLike(@PathVariable String name, HttpServletRequest request) {
        Category category = categoryService.findByName(name).orElseThrow();
        String email = request.getUserPrincipal().getName();
        User user = userService.findUserByEmail(email).orElseThrow();
        user.getUserFavoritesCategory().add(category);
        userService.update(user);
        return "redirect:/categoryInfo/{name}";
    }

    /**
     * This method deletes the instance of the user and the actual category to help the algorithm
     *
     * @param name    the category name
     * @param request
     * @return /categoryInfo/{name}
     */

    @PostMapping("/categoryInfo/{name}/dislike")
    public String categoryDislike(@PathVariable String name, HttpServletRequest request) {
        Category category = categoryService.findByName(name).orElseThrow();
        String email = request.getUserPrincipal().getName();
        User user = userService.findUserByEmail(email).orElseThrow();
        user.getUserFavoritesCategory().remove(category);
        category.setLikedByUser(false);
        userService.update(user);
        return "redirect:/categoryInfo/{name}";
    }


    @PostMapping("/addCategory")
    public String addCategory(@RequestParam String name, @RequestParam String des, @RequestParam MultipartFile icon, @RequestParam String color) throws IOException {

        boolean error;
        Category category = new Category(name, des, icon, color);
        error = categoryService.findByName(category.getName()).isPresent();
        if (!error) {
            categoryService.save(category);
        }

        return "redirect:/categories";

    }

    /**
     * This is the controller that receives the button signal and charges the Edit Screen correspondent
     *
     * @param model
     * @param categoryName
     * @param request
     * @return EditScreen.html
     */
    @PostMapping("/editCategory/{categoryName}")
    public String goToeditCategory(Model model, @PathVariable String categoryName, HttpServletRequest request) {
        Category category = categoryService.findByName(categoryName).orElseThrow();
        model.addAttribute("category", category);
        model.addAttribute("isProfile", false);
        model.addAttribute("isCategory", true);
        model.addAttribute("isPlan", false);
        return "EditScreen";
    }

    /**
     * This method applies the submit of the correspondent form and saves the new values for the instance previously created
     *
     * @param categoryName   the category name on the url to know which one user will modify
     * @param newDescription the new description of the category
     * @param color
     * @param imageFile      the new image from users computer
     * @return Profile.html
     * @throws IOException if didn't find the image
     */

    @PostMapping("/editCategory/{categoryName}/completed")
    public String editCategory(@PathVariable String categoryName,
                               @RequestParam String newDescription, @RequestParam String color, MultipartFile imageFile) throws IOException {


        Optional<Category> op = categoryService.findByName(categoryName);
        if (op.isPresent()) {
            Category category = op.get();
            categoryService.editCategory(category, newDescription, color, imageFile);
            return "redirect:/profile";

        }
            return "redirect:/categories";
    }
}