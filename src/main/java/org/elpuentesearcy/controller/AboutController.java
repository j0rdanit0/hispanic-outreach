package org.elpuentesearcy.controller;

import org.elpuentesearcy.configuration.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController
{
    @Autowired
    private Properties properties;

    @GetMapping( "/about" )
    public String about( Model model )
    {
        model.addAttribute( "boardOfDirectors", properties.getBoard() );
        model.addAttribute( "staff", properties.getStaff() );
        return "about";
    }
}
