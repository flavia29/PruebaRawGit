package es.dawequipo3.growing.controller;

import es.dawequipo3.growing.model.Category;
import es.dawequipo3.growing.model.User;
import es.dawequipo3.growing.service.CategoryService;
import es.dawequipo3.growing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

@Controller
public class ImageController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;


    /**
     * This method allows the page to display the correspondent image, getting it from the user profile image (user.getImageFile())
     *
     * @param request
     * @return ResponseEntity<Object>
     * @throws SQLException if image not found
     */
    @GetMapping("/image/profile")
    public ResponseEntity<Object> downloadProfileImage(HttpServletRequest request) throws SQLException {

        String email = request.getUserPrincipal().getName();
        User user = userService.findUserByEmail(email).orElseThrow();

        if (user.getImageFile() != null) {

            Resource file = new InputStreamResource(user.getImageFile().getBinaryStream());

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .contentLength(user.getImageFile().length()).body(file);

        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * This one is almost the same, only changes the origin of the photo, getting it from the category icon
     *
     * @param categoryName name of the category which its icon will be displayed
     * @param request
     * @return ResponseEntity<Object>
     * @throws SQLException if image not found
     */
    @GetMapping("/image/category/{categoryName}")
    public ResponseEntity<Object> downloadCategoryImage(@PathVariable String categoryName, HttpServletRequest request) throws SQLException {

        Category category = categoryService.findByName(categoryName).orElseThrow();

        if (category.getIcon() != null) {

            Resource file = new InputStreamResource(category.getIcon().getBinaryStream());

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .contentLength(category.getIcon().length()).body(file);

        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
