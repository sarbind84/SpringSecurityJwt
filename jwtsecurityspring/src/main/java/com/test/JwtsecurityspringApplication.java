package com.test;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;



import com.test.filters.JwtRequestFilter;
import com.test.models.AuthenticationRequest;
import com.test.models.AuthenticationResponse;
import com.test.util.JwtUtil;
@SpringBootApplication
public class JwtsecurityspringApplication {

	public static void main(String[] args) {
		SpringApplication.run(JwtsecurityspringApplication.class, args);
	}

}


//Run with this URL Post: http://localhost:8080/authenticate
//Update user name:xyz, pwd:xyz
//S2: using JWT auth bearer token : http://localhost:8080/hello


@RestController
class HelloWorldController {



@Autowired
private AuthenticationManager authenticationManager;



@Autowired
private JwtUtil jwtTokenUtil;



@Autowired
private MyUserDetailsService userDetailsService;



@RequestMapping({ "/hello" })
public String firstPage() {
return "Hello World";
}




//for generating tokens
@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {



try {
authenticationManager.authenticate(
new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
);
}
catch (BadCredentialsException e) {
throw new Exception("Incorrect username or password", e);
}




final UserDetails userDetails = userDetailsService
.loadUserByUsername(authenticationRequest.getUsername());



final String jwt = jwtTokenUtil.generateToken(userDetails);


//response okay and with token as response
return ResponseEntity.ok(new AuthenticationResponse(jwt));
}



}



@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
@Autowired
private UserDetailsService myUserDetailsService;
@Autowired
private JwtRequestFilter jwtRequestFilter;



@Autowired
public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
auth.userDetailsService(myUserDetailsService);
}



@Bean
public PasswordEncoder passwordEncoder() {
return NoOpPasswordEncoder.getInstance();
}



@Override
@Bean
public AuthenticationManager authenticationManagerBean() throws Exception {
return super.authenticationManagerBean();
}



@Override
protected void configure(HttpSecurity httpSecurity) throws Exception {
httpSecurity.csrf().disable()
.authorizeRequests().antMatchers("/authenticate").permitAll().
anyRequest().authenticated().and().
exceptionHandling().and().sessionManagement()
.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);



}



}