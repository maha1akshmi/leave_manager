package leave_manager.leave_manager.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    private String department;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;  // optional — only update if provided
}