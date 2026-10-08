import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserManagementService } from '../users/user-management.service';

@Component({selector:'app-activate',standalone:true,imports:[CommonModule,ReactiveFormsModule,MatButtonModule,MatFormFieldModule,MatInputModule,MatSnackBarModule],template:`<section><h1>Ativar conta</h1><form [formGroup]="form" (ngSubmit)="submit()"><mat-form-field><mat-label>Nova senha</mat-label><input matInput type="password" formControlName="password"></mat-form-field><mat-form-field><mat-label>Confirmar senha</mat-label><input matInput type="password" formControlName="confirmation"></mat-form-field><p *ngIf="form.value.password !== form.value.confirmation && form.get('confirmation')?.touched">As senhas não conferem.</p><button mat-raised-button [disabled]="form.invalid || form.value.password !== form.value.confirmation || saving">Ativar conta</button></form></section>`})
export class ActivateComponent {
 readonly form; readonly token:string; saving=false;
 constructor(private readonly fb:FormBuilder,private readonly route:ActivatedRoute,private readonly service:UserManagementService,private readonly router:Router,private readonly snack:MatSnackBar,location:Location){this.token=this.route.snapshot.queryParamMap.get('token')||'';if(this.token) location.replaceState('/activate');this.form=this.fb.group({password:['',[Validators.required,Validators.minLength(8),Validators.maxLength(128)]],confirmation:['',Validators.required]});}
 submit():void{if(this.form.invalid||this.saving||!this.token||this.form.value.password!==this.form.value.confirmation)return;this.saving=true;this.service.activate(this.token,this.form.value.password!).subscribe({next:()=>{this.snack.open('Conta ativada. Faça login.','Fechar');this.router.navigate(['/login']);},error:()=>{this.saving=false;this.snack.open('Link inválido ou expirado.','Fechar');}});}
}
