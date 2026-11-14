import { Component , OnInit } from '@angular/core';

@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  styleUrl: './navbarcomponent.css',
  templateUrl: './navbarcomponent.html',
})
export class Navbarcomponent implements OnInit {

  userRole: 'admin' | 'user' = 'admin';

  constructor() { }

  ngOnInit(): void {

  }
}
