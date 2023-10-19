package br.com.brunodias.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.brunodias.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/tasks")
public class TaskController {
    
    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){
        // Set user id
        UUID idUser = (UUID) request.getAttribute("idUser");
        taskModel.setIdUser(idUser);

        // Checar data
        LocalDateTime currentTime = LocalDateTime.now();
        if(currentTime.isAfter(taskModel.getStartAt()) || taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.badRequest().body("Data inválida");
        }

        // Salvar
        TaskModel task = this.taskRepository.save(taskModel);
        return ResponseEntity.ok().body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request){
        UUID userId = (UUID) request.getAttribute("idUser");
        return taskRepository.findByIdUser(userId);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {

        // Get task from database
        TaskModel task = this.taskRepository.findById(id).orElse(null);
        
        // Check if task id and idUser are valid
        UUID idUser = (UUID) request.getAttribute("idUser");
        if (task == null || (!task.getIdUser().equals(idUser))){
            return ResponseEntity.badRequest().body("Task não encontrada");
        }

        // Copy properties
        Utils.copyNonNullProperties(taskModel, task);

        // Save updated task
        TaskModel taskSaved = this.taskRepository.save(task);
        return ResponseEntity.ok().body(taskSaved);
    }
}
