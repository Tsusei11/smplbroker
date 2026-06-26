package pl.edu.pjatk.mas.s29904.simplebroker.web.advice;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pl.edu.pjatk.mas.s29904.simplebroker.models.clients.Person;
import pl.edu.pjatk.mas.s29904.simplebroker.repositories.ClientRepository;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final ClientRepository clientRepository;
    private static final Long CURRENT_CLIENT_ID = 1L;

    @ModelAttribute("currentClientName")
    public String populateCurrentClientName() {
        return clientRepository.findById(CURRENT_CLIENT_ID)
                .map(client -> {
                    if (client instanceof Person p){
                        return p.getFirstName() + " " + p.getLastName();
                    }
                    return client.getEmail();
                })
                .orElse("Gość");
    }
}
