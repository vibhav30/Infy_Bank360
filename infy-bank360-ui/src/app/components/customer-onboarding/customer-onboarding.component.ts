import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer.service';
import { Customer } from '../../models/customer.model';
import { ErrorInfo } from '../../models/error-info.model';

@Component({
  selector: 'app-customer-onboarding',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './customer-onboarding.component.html',
  styleUrls: ['./customer-onboarding.component.css']
})
export class CustomerOnboardingComponent implements OnInit {
  onboardingForm!: FormGroup;
  isSubmitting = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  createdCustomer: Customer | null = null;

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.onboardingForm = this.fb.group({
      fullName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(100)
        ]
      ],
      email: [
        '',
        [
          Validators.required,
          Validators.email,
          Validators.maxLength(120)
        ]
      ],
      mobileNumber: [
        '',
        [
          Validators.required,
          Validators.pattern('^[6-9][0-9]{9}$')
        ]
      ],
      nationalId: [
        '',
        [
          Validators.required,
          Validators.pattern('^[A-Z0-9]{8,16}$')
        ]
      ]
    });
  }

  get f() {
    return this.onboardingForm.controls;
  }

  onSubmit(): void {
    if (this.onboardingForm.invalid) {
      this.onboardingForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = null;
    this.errorMessage = null;

    const newCustomer: Customer = this.onboardingForm.value;

    this.customerService.registerCustomer(newCustomer).subscribe({
      next: (response: Customer) => {
        this.isSubmitting = false;
        this.createdCustomer = response;
        this.successMessage = `Customer onboarded successfully! Customer ID (CIF): ${response.customerId}`;
        this.onboardingForm.reset();
      },
      error: (err) => {
        this.isSubmitting = false;
        if (err.error && err.error.errorMessage) {
          const errorInfo: ErrorInfo = err.error;
          this.errorMessage = `[${errorInfo.errorCode || 'VALIDATION_ERROR'}] ${errorInfo.errorMessage}`;
        } else {
          this.errorMessage = 'An unexpected network error occurred while connecting to API Gateway (Port 8080).';
        }
      }
    });
  }

  dismissAlert(): void {
    this.successMessage = null;
    this.errorMessage = null;
  }
}
