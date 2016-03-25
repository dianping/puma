package com.dianping.puma.web.web;

import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dto.ClientDto;
import com.dianping.puma.common.model.Client;
import com.dianping.puma.common.service.PumaClientService;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = {"/clients"})
public class PumaClientController extends BasicController {

    @Autowired
    Converter converter;

    @Autowired
    PumaClientService pumaClientService;

    @RequestMapping(value = "/{clientName}", method = RequestMethod.GET)
    @ResponseBody
    public ClientDto readByParam(@PathVariable("clientName") String clientName) {
        Client client = pumaClientService.findByClientName(clientName);
        return converter.convert(client, ClientDto.class);
    }

    @RequestMapping(value = "/{clientName}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    public void createByParam(@PathVariable("clientName") String clientName, @RequestBody ClientDto clientDto) {
    }

    @RequestMapping(value = "/{clientName}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    public void updateByParam(@PathVariable("clientName") String clientName, @RequestBody ClientDto clientDto) {
        clientDto.setClientName(clientName);
        Client client = converter.convert(clientDto, Client.class);
        pumaClientService.update(client);
    }

    @RequestMapping(value = "/{clientName}", method = RequestMethod.DELETE)
    public void removeByParam(@PathVariable("clientName") String clientName) {
        pumaClientService.remove(clientName);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<ClientDto> read() {
        List<Client> clients = pumaClientService.findAll();
        return converter.convert(clients, new TypeToken<List<ClientDto>>() {
        }.getType());
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void create(@RequestBody ClientDto clientDto) {
        Client client = converter.convert(clientDto, Client.class);
        pumaClientService.create(client);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    public void update(@RequestBody ClientDto clientDto) {
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    public void remove() {
    }
}
