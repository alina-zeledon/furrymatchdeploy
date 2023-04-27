import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import dayjs from 'dayjs/esm';

import { ContractFormService, ContractFormGroup } from './contract-form.service';
import { IContract } from '../contract.model';
import { ContractService } from '../service/contract.service';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';

@Component({
  selector: 'jhi-contract-update',
  templateUrl: './contract-update.component.html',
  styleUrls: ['./contract-form.component.css'],
})
export class ContractUpdateComponent implements OnInit {
  isSaving = false;
  contract: IContract | null = null;

  editForm: ContractFormGroup = this.contractFormService.createContractFormGroup();

  constructor(
    protected contractService: ContractService,
    protected contractFormService: ContractFormService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ contract }) => {
      this.contract = contract;
      if (contract) {
        contract.otherNotes = contract.otherNotes.slice(0, contract.otherNotes.indexOf(';'));
        this.updateForm(contract);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const contract = this.contractFormService.getContract(this.editForm);
    contract.contractDate = dayjs();
    if (contract.id !== null) {
      this.subscribeToSaveResponse(this.contractService.update(contract));
    } else {
      this.subscribeToSaveResponse(this.contractService.create(contract));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IContract>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    Swal.fire({
      title: '¡Contrato generado satisfactoriamente!',
      text: 'Revisa el contrato que ha sido enviado a tu correo electrónico.Te recomendamos imprimirlo, firmarlo y adjuntar las fotocopias de las cédulas de identidad de ambas partes.',
      icon: 'success',
      confirmButtonColor: '#3381f6',
      confirmButtonText: 'Cerrar',
    }).then((result: any) => {
      this.router.navigate(['/contract']);
    });
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(contract: IContract): void {
    this.contract = contract;
    this.contractFormService.resetForm(this.editForm, contract);
  }
}
