import { Component, OnInit, inject } from '@angular/core';
import { UsersService } from './users.service';
import { UserDto, UserRole } from '../../core/models/user.model';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Observable, startWith, map } from 'rxjs';
import { CommonModule, AsyncPipe, NgIf, NgFor } from '@angular/common';
import { PageContainerComponent } from '../../shared/components/page-container/page-container.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AsyncPipe, PageContainerComponent, LoadingSpinnerComponent],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {
  private usersService = inject(UsersService);

  users: UserDto[] = [];
  filteredUsers$!: Observable<UserDto[]>;
  isLoading = true;

  filterForm = new FormGroup({
    search: new FormControl(''),
    role: new FormControl<UserRole | 'ALL'>('ALL'),
    access: new FormControl<'ALL' | 'ACTIVE' | 'EXPIRED'>('ALL')
  });

  ngOnInit() {
    this.usersService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
        this.initFiltering();
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  private initFiltering() {
    this.filteredUsers$ = this.filterForm.valueChanges.pipe(
      startWith(this.filterForm.value),
      map(filters => this.applyFilters(filters))
    );
  }

  private applyFilters(f: any): UserDto[] {
    return this.users.filter(u => {
      const matchSearch = !f.search ||
        u.firstName?.toLowerCase().includes(f.search.toLowerCase()) ||
        u.username?.toLowerCase().includes(f.search.toLowerCase()) ||
        u.telegramId?.toString().includes(f.search);

      const matchRole = f.role === 'ALL' || u.role === f.role;

      let matchAccess = true;
      if (f.access === 'ACTIVE') {
        matchAccess = !!u.manualAccessUntil && new Date(u.manualAccessUntil) > new Date();
      } else if (f.access === 'EXPIRED') {
        matchAccess = !u.manualAccessUntil || new Date(u.manualAccessUntil) <= new Date();
      }

      return matchSearch && matchRole && matchAccess;
    });
  }
}
