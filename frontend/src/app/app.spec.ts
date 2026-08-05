import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';

describe('App',()=>{it('explains the asynchronous checkout seam',async()=>{await TestBed.configureTestingModule({imports:[App],providers:[provideHttpClient(),provideHttpClientTesting()]}).compileComponents();const fixture=TestBed.createComponent(App);fixture.detectChanges();expect(fixture.nativeElement.textContent).toContain('Checkout Control Room');expect(fixture.nativeElement.textContent).toContain('Enviar checkout assíncrono');});});
