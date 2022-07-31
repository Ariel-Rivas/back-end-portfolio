
package com.Portfolio.Ariel.SecurityController;

import com.Portfolio.Ariel.Security.Entity.Rol;
import com.Portfolio.Ariel.Security.Entity.Usuario;
import com.Portfolio.Ariel.Security.Enum.RolNombre;
import com.Portfolio.Ariel.Security.Jwt.JwtProvider;
import com.Portfolio.Ariel.Security.Service.RolService;
import com.Portfolio.Ariel.Security.Service.UsuarioService;
import com.Portfolio.Ariel.SecurityDto.JwtDto;
import com.Portfolio.Ariel.SecurityDto.LoginUsuario;
import com.Portfolio.Ariel.SecurityDto.NuevoUsuario;
import java.util.HashSet;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping ("/auth")
@CrossOrigin
public class AuthController {
   
    
     @Autowired  
     PasswordEncoder passwordEncoder;
     
     @Autowired  
     AuthenticationManager authenticationManager; 
     
     @Autowired  
     UsuarioService usuarioService; 
     
     @Autowired 
     RolService rolServicie; 
     
     @Autowired 
     JwtProvider jwtProvider; 
     
     @PostMapping("/nuevo")
     public ResponseEntity<?>nuevo(@Valid @RequestBody NuevoUsuario nuevoUsuario,BindingResult bindingResult){
         if (bindingResult.hasErrors())
            return new ResponseEntity(new Mensaje("campos incorrectos o email invalido"), HttpStatus.BAD_REQUEST);
     
     
         if (usuarioService.existsByNombreUsuario(nuevoUsuario.getNombreUsuario()))
            return new ResponseEntity(new Mensaje("Ese nombre ya existe"), HttpStatus.BAD_REQUEST); 
            
            
         if (usuarioService.existsByEmail(nuevoUsuario.getEmail()))
            return new ResponseEntity(new Mensaje("Ese email ya existe"),HttpStatus.BAD_REQUEST); 
            
          Usuario usuario = new Usuario(nuevoUsuario.getNombre(),nuevoUsuario.getNombreUsuario(),   
            nuevoUsuario.getEmail(),passwordEncoder.encode(nuevoUsuario.getPassword()));
          
          
          Set<Rol> roles = new HashSet<>(); 
          roles.add(rolServicie.getByRolNombe(RolNombre.RoleUser).get());
          if(nuevoUsuario.getRoles().contains("admin")) 
              roles.add(rolServicie.getByRolNombe(RolNombre.RoleAdmin).get()); 
              usuario.setRoles(roles);
              usuarioService.save(usuario);
              return new ResponseEntity(new Mensaje("Usuario guardado"),HttpStatus.CREATED);
     }
         @PostMapping("/login")
             public ResponseEntity<JwtDto> login(@Valid @RequestBody LoginUsuario loguinUsuario ,BindingResult bindingResult){
                 
                 
                 if (bindingResult.hasErrors())
                     return new ResponseEntity( new Mensaje("campos mal puestos "), HttpStatus.BAD_REQUEST);
                     
                     Authentication   authentication =   authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                     loguinUsuario.getNombreUsuario(),loguinUsuario.getPassword())); 
                     
                     SecurityContextHolder.getContext().setAuthentication (authentication);
                     
                     String jwt = jwtProvider.generateToken(authentication);
                     
                     UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                     
                     JwtDto jwtDto = new JwtDto(jwt,userDetails.getUsername(),userDetails.getAuthorities());
            
             
                     return new ResponseEntity(jwtDto, HttpStatus.OK); 
             
             
             
             }
           
}           
                     
                   