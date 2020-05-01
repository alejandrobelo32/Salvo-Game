package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.entities.*;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
        return (args) -> {
            // save a couple of customers

            Player player1 = new Player("Jack@gmail.com",passwordEncoder().encode("ahre"));
            Player player2 = new Player("Chloe@gmail.com", passwordEncoder().encode("ahre"));

            playerRepository.save(player1);
            playerRepository.save(player2);

            Game game1= new Game();

            gameRepository.save(game1);

            GamePlayer gamePlayer1= new GamePlayer(game1, player1);
            GamePlayer gamePlayer2= new GamePlayer(game1, player2);

            gamePlayerRepository.save(gamePlayer1);
            gamePlayerRepository.save(gamePlayer2);

            Ship ship1 = new Ship(gamePlayer1, Arrays.asList("E1","F1","G1"),"Submarine");
            Ship ship2 = new Ship(gamePlayer1, Arrays.asList("B5","C5","D5"),"Destroyer");

            shipRepository.save(ship1);
            shipRepository.save(ship2);

            Ship ship3 = new Ship(gamePlayer2, Arrays.asList("E1","F1","G1"),"Submarine");
            Ship ship4 = new Ship(gamePlayer2, Arrays.asList("B5","C5","D5"),"Destroyer");

            shipRepository.save(ship3);
            shipRepository.save(ship4);

            Salvo salvo1 = new Salvo(gamePlayer2, Arrays.asList("C4","C5","C6"),1);
            Salvo salvo2 = new Salvo(gamePlayer2, Arrays.asList("F1","F2","F3"),1);

            salvoRepository.save(salvo1);
            salvoRepository.save(salvo2);

            Salvo salvo3 = new Salvo(gamePlayer1, Arrays.asList("B10","C10","D10"),1);
            Salvo salvo4 = new Salvo(gamePlayer1, Arrays.asList("D5","E5","F5"),1);

            salvoRepository.save(salvo3);
            salvoRepository.save(salvo4);

            Score score1= new Score(game1, player1, 1);
            Score score2= new Score(game1, player2, 0);

            scoreRepository.save(score1);
            scoreRepository.save(score2);

            Game game2= new Game();

            gameRepository.save(game2);

            GamePlayer gamePlayer3 = new GamePlayer(game2, player1);

            gamePlayerRepository.save(gamePlayer3);

        };

    }

}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(inputName-> {
            Player player = playerRepository.findByUserName(inputName);
            if (player != null) {
                return new User(player.getUserName(), player.getPassword(),
                        AuthorityUtils.createAuthorityList("USER"));
            } else {
                throw new UsernameNotFoundException("Unknown user: " + inputName);
            }
        });
    }
}

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/web/**", "/api/games", "/api/login","/api/leaderboard","/api/players").permitAll()
                .antMatchers("/api/**", "/web/game.html**").hasAuthority("USER")
                //.antMatchers("/rest/**").hasAnyAuthority("ADMIN")
                .anyRequest().permitAll();

        http.formLogin()
                .usernameParameter("username")
                .passwordParameter("password")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");

        // turn off checking for CSRF tokens
        http.csrf().disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }

    }
}