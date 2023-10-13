package com.smart.smartcontactmanager.controllers;

import com.smart.smartcontactmanager.entities.Contact;
import com.smart.smartcontactmanager.entities.User;
import com.smart.smartcontactmanager.helper.Message;
import com.smart.smartcontactmanager.repositories.ContactRepository;
import com.smart.smartcontactmanager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    //method to add common data
    @ModelAttribute
    public  void addCommonData(Model model, Principal principal){

        String userName = principal.getName();
        System.out.println("USERNAME "+userName);

        //get the user using username(Email)

        User user = userRepository.getUserByUserName(userName);

        System.out.println("USER "+user);

        model.addAttribute("user", user);

    }

    //dashboard Home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal){

        model.addAttribute("title","User Dashboard");

        return "normal/user_dashboard";


    }

    //open add form handler

    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){

        model.addAttribute("title","Add Contact");
        model.addAttribute("contact", new Contact());

        return "normal/add_contact_form";
    }

    //processing add contact form

    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file, Principal principle,
                                 HttpSession session){

        try{

            String name = principle.getName();
            User user = this.userRepository.getUserByUserName(name);

            //processing and uploading file

            if (file.isEmpty()){

                System.out.println("File is empty");
                contact.setImage("contact.png");


            }else {
                contact.setImage(file.getOriginalFilename());

                File file1 = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(file1.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);

                System.out.println("You Image has been uploaded");




            }

            user.getContacts().add(contact);

            contact.setUser(user);

            this.userRepository.save(user);

            System.out.println("DATA"+ contact);
            System.out.println("Added to database");

            //success message
            session.setAttribute("message",new Message("Your contact has been added successfully !! add new contact..","success"));

        }catch (Exception e){

            System.out.println("ERROR"+e.getMessage());
            e.printStackTrace();

            //error message
            session.setAttribute("message",new Message("something went wrong!! try again..","danger"));


        }

        return "normal/add_contact_form";
    }

    //show contact handler
    //per page 5
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal){

        m.addAttribute("title","Show User Contacts");

        //contacts list

        //String userName = principal.getName();
        //User user = this.userRepository.getUserByUserName(userName);
        //List<Contact> contacts = user.getContacts();

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        Pageable pageable = PageRequest.of(page, 5);

        Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
        m.addAttribute("contacts", contacts);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", contacts.getTotalPages());


        return "normal/show-contacts";
    }

    //showing particular contact details
    @RequestMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal){

        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        if (user.getId()==contact.getUser().getId()){

            model.addAttribute("contact", contact);
            model.addAttribute("title",contact.getName());

        }

        return "normal/contact_detail";


    }

    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid") Integer cId,
                                Model model,
                                HttpSession session,
                                Principal principal){

        System.out.println("CID"+cId);

        Contact contact = this.contactRepository.findById(cId).get();
        //check...
        System.out.println("Contact"+contact.getcId());


        User user = this.userRepository.getUserByUserName(principal.getName());

        user.getContacts().remove(contact);

        this.userRepository.save(user);



        System.out.println("Deleted");

        session.setAttribute("message", new Message("Contact has been deleted...","success"));

        return "redirect:/user/show-contacts/0";
    }

    //open update contact form handler
    @PostMapping("/update-contact/{cid}")
    public String updateForm(@PathVariable("cid") Integer cid, Model m){

        m.addAttribute("title", "Update Contact");
        Contact contact = this.contactRepository.findById(cid).get();
        m.addAttribute("contact", contact);

        return "normal/update_form";

    }

    //update contact handler
    @RequestMapping(value="/process-update",method = RequestMethod.POST)//@post method is easy
    public String updateHandler(@ModelAttribute Contact contact,
                                @RequestParam("profileImage")MultipartFile file,
                                Model m, HttpSession session,
                                Principal principal){

        try {

            //old contact details
            Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();

            if (!file.isEmpty()){
                //filework...
                //rewrite
                //delete old photo
                File file2 = new ClassPathResource("static/img").getFile();
                File file3 = new File(file2, oldcontactDetail.getImage());
                file3.delete();

                //update new photo

                File file1 = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(file1.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);

                contact.setImage(file.getOriginalFilename());
            }else{
                contact.setImage(oldcontactDetail.getImage());
            }

            User user = this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);

            this.contactRepository.save(contact);

            session.setAttribute("message", new Message("Your Contact is updated!","success"));

        }catch(Exception e){
            e.printStackTrace();
        }




        return "redirect:/user/"+contact.getcId()+"/contact";
    }

    //your profile
    @RequestMapping("/profile")
    public String yourProfile(Model model){
        model.addAttribute("title","Profile Page");
        return "normal/profile";
    }

}
