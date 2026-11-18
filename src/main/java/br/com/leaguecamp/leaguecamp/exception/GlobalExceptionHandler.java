package br.com.leaguecamp.leaguecamp.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Captura erros de banco que não foram tratados nos controllers
 * e mostra uma página de erro com a mensagem original do PostgreSQL.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView tratarErroDeBanco(DataAccessException erro) {
        ModelAndView pagina = new ModelAndView("erro");
        pagina.addObject("titulo", "Erro no banco de dados");
        pagina.addObject("mensagem", erro.getMostSpecificCause().getMessage());
        return pagina;
    }
}
