import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { SessionStorageService } from 'ngx-webstorage';
import { HttpResponse } from '@angular/common/http';
import { interval, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { OnDestroy } from '@angular/core';
import { VERSION } from 'app/app.constants';
import { LANGUAGES } from 'app/config/language.constants';
import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import { ProfileService } from 'app/layouts/profiles/profile.service';
import { EntityNavbarItems } from 'app/entities/entity-navbar-items';
import { filter } from 'rxjs/operators';

const petsListRoute = /^\/pet$/;
const searchCriteriaNewRoute = /^\/search-criteria\/new(\/.*)?$/;
const petNewRoute = /^\/pet\/new$/;

import { ChatService } from '../../entities/chat/service/chat.service';
import { IChat } from '../../entities/chat/chat.model';

@Component({
  selector: 'jhi-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent implements OnInit, OnDestroy {
  inProduction?: boolean;
  isNavbarCollapsed = true;
  languages = LANGUAGES;
  openAPIEnabled?: boolean;
  version = '';
  account: Account | null = null;
  entitiesNavbarItems: any[] = [];
  unreadChats: IChat[] = []; // Define the unreadChats property here

  activeImgProfile = '../content/images/btn_profile_active.png';
  inactiveImgProfile = '../content/images/btn_profile_inactive.png';
  currentImgProfile = this.inactiveImgProfile;

  activeImgChat = '../content/images/btn_chat_active.png';
  inactiveImgChat = '../content/images/btn_chat_inactive.png';
  currentImgChat = this.inactiveImgChat;
  isHoveringChat = false;

  activeImgFind = '../content/images/btn_matches_active.png';
  inactiveImgFind = '../content/images/btn_matches_inactive.png';
  currentImgFind = this.inactiveImgFind;
  isHoveringFind = false;

  activeImgMatch = '../content/images/btn_find_active.png';
  inactiveImgMatch = '../content/images/btn_find_inactive.png';
  currentImgMatch = this.inactiveImgMatch;
  isHoveringMatches = false;

  activeImgContract = '../content/images/btn_contract_active.png';
  inactiveImgContract = '../content/images/btn_contract_inactive.png';
  currentImgContract = this.inactiveImgContract;
  isHoveringContract = false;

  unreadImgChat = '../content/images/btn_chat_notification.png';
  userMenuIsOpen = false;
  adminMenuIsOpen = false;
  private destroy$ = new Subject<void>();

  showIcons = true;

  constructor(
    private loginService: LoginService,
    private translateService: TranslateService,
    private sessionStorageService: SessionStorageService,
    private accountService: AccountService,
    private profileService: ProfileService,
    private chatService: ChatService,
    private router: Router
  ) {
    if (VERSION) {
      this.version = VERSION.toLowerCase().startsWith('v') ? VERSION : `v${VERSION}`;
    }
    this.updateUserIcon();
    this.updateMatchIcon();
    this.updateChatIcon();
    this.updateFindIcon();
    this.updateContractIcon();
  }

  ngOnInit(): void {
    this.entitiesNavbarItems = EntityNavbarItems;
    this.profileService.getProfileInfo().subscribe(profileInfo => {
      this.inProduction = profileInfo.inProduction;
      this.openAPIEnabled = profileInfo.openAPIEnabled;
    });

    this.accountService.getAuthenticationState().subscribe(account => {
      this.account = account;
    });

    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.currentImgProfile = this.isRouteActive('/pet') ? this.activeImgProfile : this.inactiveImgProfile;
      }
    });

    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.updateMatchIcon();
      }
    });

    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.updateChatIcon();
      }
    });

    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.updateFindIcon();
      }
    });

    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.updateContractIcon();
      }
    });
    /*this.chatService.chatRead.subscribe(() => {
      this.loadUnreadChats();
    });*/

    interval(5000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (this.accountService.isAuthenticated()) {
          this.loadUnreadChats();
        }
      });

    if (this.accountService.isAuthenticated()) {
      this.loadUnreadChats();
    }
  }
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  isUserRouteActive(): boolean {
    const currentUrl = this.router.url;
    return currentUrl.includes('pet');
  }

  updateUserIcon() {
    if (this.isUserRouteActive()) {
      this.currentImgProfile = this.activeImgProfile;
    } else {
      this.currentImgProfile = this.inactiveImgProfile;
    }
  }

  isMatchRouteActive(): boolean {
    const currentUrl = this.router.url;
    return currentUrl.includes('/match');
  }

  updateMatchIcon() {
    if (this.isMatchRouteActive() || this.isHoveringMatches) {
      this.currentImgMatch = this.activeImgMatch;
    } else {
      this.currentImgMatch = this.inactiveImgMatch;
    }
  }

  isChatRouteActive(): boolean {
    const currentUrl = this.router.url;
    return currentUrl.includes('/chat');
  }

  loadUnreadChats(): void {
    this.chatService.getUnreadChatsForCurrentUser().subscribe((res: HttpResponse<IChat[]>) => {
      this.unreadChats = res.body || [];
      //console.log('Unread chats: ' + JSON.stringify(this.unreadChats, null, 2));
      this.updateChatIcon();
    });
  }
  updateChatIcon() {
    if (this.isChatRouteActive() || this.isHoveringChat) {
      this.currentImgChat = this.activeImgChat;
    } else if (this.unreadChats.length > 0) {
      // If there are unread chats, set the chat icon to a different image to indicate new messages
      this.currentImgChat = this.unreadImgChat;
    } else {
      this.currentImgChat = this.inactiveImgChat;
    }
  }
  isFindRouteActive(): boolean {
    const currentUrl = this.router.url;
    return currentUrl.includes('pet/search');
  }

  updateFindIcon() {
    if (this.isFindRouteActive() || this.isHoveringFind) {
      this.currentImgFind = this.activeImgFind;
    } else {
      this.currentImgFind = this.inactiveImgFind;
    }
  }

  isContractRouteActive(): boolean {
    const currentUrl = this.router.url;
    return currentUrl.includes('contract');
  }

  updateContractIcon() {
    if (this.isContractRouteActive() || this.isHoveringContract) {
      this.currentImgContract = this.activeImgContract;
    } else {
      this.currentImgContract = this.inactiveImgContract;
    }
  }

  isRouteActive(route: string): boolean {
    return this.router.url === route;
  }

  toggleUserDropdown(event: MouseEvent): void {
    event.stopPropagation();
    this.userMenuIsOpen = !this.userMenuIsOpen;
    this.adminMenuIsOpen = false;
  }

  toggleAdminDropdown(event: MouseEvent): void {
    event.stopPropagation();
    this.adminMenuIsOpen = !this.adminMenuIsOpen;
    this.userMenuIsOpen = false;
  }

  hideUserDropdown(): void {
    this.userMenuIsOpen = false;
  }

  hideAdminDropdown(): void {
    this.adminMenuIsOpen = false;
  }

  changeLanguage(languageKey: string): void {
    this.sessionStorageService.store('locale', languageKey);
    this.translateService.use(languageKey);
  }

  collapseNavbar(): void {
    this.isNavbarCollapsed = true;
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  logout(): void {
    this.collapseNavbar();
    this.loginService.logout();
    this.router.navigate(['']);
  }

  toggleNavbar(): void {
    this.isNavbarCollapsed = !this.isNavbarCollapsed;
  }

  goToChat(): void {
    this.router.navigate(['/chat']);
  }

  get shouldShowIcons(): boolean {
    return !this.isPetsListRouteActive();
  }

  isPetsListRouteActive(): boolean {
    const currentUrl = this.router.url;
    const isPetListRoute = petsListRoute.test(currentUrl);
    const isNewSearchCriteriaRoute = searchCriteriaNewRoute.test(currentUrl);
    const isNewPetRoute = petNewRoute.test(currentUrl);

    // Devuelve true si está en la página de lista de mascotas o en la página para crear nuevos criterios de búsqueda
    return isPetListRoute || isNewSearchCriteriaRoute || isNewPetRoute;
  }
}
