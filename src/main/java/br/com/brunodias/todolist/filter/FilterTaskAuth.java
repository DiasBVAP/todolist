package br.com.brunodias.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import br.com.brunodias.todolist.user.IUserRepository;
import br.com.brunodias.todolist.user.UserModel;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            
        // Checar a path
        String serverletPath = request.getServletPath();
        if(!serverletPath.equals("/tasks/")){
            filterChain.doFilter(request, response);
        }
    
        // Receber auth
        String authEncoded = request.getHeader("Authorization")
        .substring("Basic".length()).trim();

        // Decode auth
        String authDecoded = new String(Base64.getDecoder().decode(authEncoded));
        String[] credentials = authDecoded.split(":");
        String username = credentials[0];
        String password = credentials[1];

        // Validar usuário
        UserModel user = this.userRepository.findByUsername(username);
        if(user == null){
            response.sendError(401, "Usuário ou senha incorretos");
        }else{
            Result verificationResult = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
            if(verificationResult.verified){
                filterChain.doFilter(request, response);
            }else{
                response.sendError(401, "Usuário ou senha incorretos");
            }
        }
    }
}
