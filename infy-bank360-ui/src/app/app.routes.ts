import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { CustomerOnboardingComponent } from './components/customer-onboarding/customer-onboarding.component';
import { FundTransferComponent } from './components/fund-transfer/fund-transfer.component';
import { LoansComponent } from './components/loans/loans.component';
import { HelpdeskComponent } from './components/helpdesk/helpdesk.component';
import { ComplianceComponent } from './components/compliance/compliance.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, title: 'Infy_Bank360 - Executive Dashboard' },
  { path: 'onboard', component: CustomerOnboardingComponent, title: 'Infy_Bank360 - Customer Onboarding & KYC' },
  { path: 'transfer', component: FundTransferComponent, title: 'Infy_Bank360 - Fund Transfers & Bills' },
  { path: 'loans', component: LoansComponent, title: 'Infy_Bank360 - Loan Origination & EMI Simulator' },
  { path: 'helpdesk', component: HelpdeskComponent, title: 'Infy_Bank360 - Service Desk & Disputes' },
  { path: 'compliance', component: ComplianceComponent, title: 'Infy_Bank360 - AML Compliance & Audit' },
  { path: '**', redirectTo: 'dashboard' }
];
