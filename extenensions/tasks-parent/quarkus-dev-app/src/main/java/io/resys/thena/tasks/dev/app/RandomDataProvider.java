package io.resys.thena.tasks.dev.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.client.api.model.Task.Status;

public class RandomDataProvider {

  private final Random rand = new Random();
  
  private final Map<Integer, String> ROLES = Map.of(
      1, "admin-role",
      2, "water-department",
      3, "education-department",
      4, "elderly-care-department",
      5, "sanitization-department"); 
  
  private final Map<Integer, String> OWNERS = Map.of(
      1, "sam vimes",
      2, "lord vetinari",
      3, "lady sybil vimes",
      4, "carrot ironfoundersson",
      5, "nobby nobbs"); 
  
  private final Map<Integer, String> SUBJECTS = Map.of(
      1, "Request for elderly care",
      2, "School application",
      3, "Sewage water disposal",
      4, "Water well construction",
      5, "General message"); 

  private final Map<Integer, String> SSN = Map.of(
      1, "Anette Lampen - 121097-676M",
      2, "Piia-Noora Salmelainen - 131274-780A",
      3, "Arto Hakola - 170344-6999",
      4, "Kyllikki Multala - 270698-194T",
      5, "Pentti Parviainen - 171064-319U"); 
  
  
  
  public int nextInt(final int min, final int max) {
    return rand.nextInt(max - min + 1) + min;
    
  }
  
  public String getDescription() {
    return "Long text description....";
  }
  
  public String getSubject() {
    final var subject = SUBJECTS.get(nextInt(1, SUBJECTS.size()));
    final var ssn = SSN.get(nextInt(1, SSN.size()));
    return subject + " " + ssn;
  }

  public List<String> getRoles() {
    final var roles = new ArrayList<String>();
    final var groups = nextInt(1, ROLES.size());
    for(var index = 0; index < groups; index++) {
      boolean defined = false;
      do {
        final var roleId = ROLES.get(nextInt(1, ROLES.size()));
        defined = roles.contains(roleId);
        if(!defined) {
          roles.add(roleId);
        }
      } while(defined);
    }
    return roles;
  }

  
  public List<String> getOwners() {
    final var owners = new ArrayList<String>();
    final var groups = nextInt(1, OWNERS.size()) -1;
    for(var index = 0; index < groups; index++) {
      boolean defined = false;
      do {
        final var ownerId = OWNERS.get(nextInt(1, OWNERS.size()));
        defined = owners.contains(ownerId);
        if(!defined) {
          owners.add(ownerId);
        }
      } while(defined);
    }
    return owners;
  }

  public Priority getPriority() {
    final var next = nextInt(1, 3);
    switch (next) {
    case 1: return Priority.LOW;
    case 2: return Priority.MEDIUM;
    default: return Priority.HIGH;
    }
  }
  
  public Status getStatus() {
    final var next = nextInt(1, 4);
    switch (next) {
    case 1: return Status.CREATED;
    case 2: return Status.IN_PROGRESS;
    case 3: return Status.COMPLETED;
    default: return Status.REJECTED;
    }
  }
}
