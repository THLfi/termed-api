package fi.thl.termed.service;

import com.hp.hpl.jena.rdf.model.Model;

import java.util.UUID;

import fi.thl.termed.domain.User;

public interface ResourceRdfService {

  Model save(UUID schemeId, Model model, User currentUser);

}
