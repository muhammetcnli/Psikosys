package com.atlas.Psikosys.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    // Make the index.html as the default page
    @RequestMapping("/")
    public String index1() {

        return "index";
    }


}
